#!/usr/bin/env python3
"""
Sync OpenProject work packages to YAML feature files
"""
import json
import re
import os
import subprocess
from datetime import datetime

# Config
OPENPROJECT_URL = "https://aam.openproject.com"
PROJECT = "saas-sport-v2-1"

def get_token():
    config_path = os.path.expanduser("~/.sport-saas/config")
    if os.path.exists(config_path):
        with open(config_path) as f:
            for line in f:
                if line.startswith("OPENPROJECT_TOKEN="):
                    return line.strip().split("=", 1)[1]
    return None

def fetch_work_packages(token):
    url = f"{OPENPROJECT_URL}/api/v3/projects/{PROJECT}/work_packages?pageSize=300"
    result = subprocess.run(
        ["curl", "-s", "-u", f"apikey:{token}", url],
        capture_output=True, encoding='utf-8'
    )
    return json.loads(result.stdout)

def main():
    token = get_token()
    if not token:
        print("[ERROR] Token OpenProject non trouve")
        return

    print("[1/3] Recuperation des work packages...")
    data = fetch_work_packages(token)
    elements = data.get('_embedded', {}).get('elements', [])
    print(f"   {len(elements)} work packages")

    # Group by epic
    epics = {}
    tasks = {}

    for e in elements:
        wp_type = e['_links']['type']['title']
        subject = e['subject']
        wp_id = e['id']
        desc = e.get('description', {})
        desc_raw = desc.get('raw', '') if desc else ''

        # Get priority
        priority = 'medium'
        if '_links' in e and 'priority' in e['_links']:
            prio_title = e['_links']['priority'].get('title', '').lower()
            if 'high' in prio_title or 'urgent' in prio_title:
                priority = 'high'
            elif 'low' in prio_title:
                priority = 'low'

        # Get status
        status = 'new'
        if '_links' in e and 'status' in e['_links']:
            status = e['_links']['status'].get('title', 'New').lower().replace(' ', '_')

        # Get estimated time
        hours = 4
        est = e.get('estimatedTime', '')
        if est and est.startswith('PT'):
            try:
                hours = int(float(est.replace('PT', '').replace('H', '')))
            except:
                hours = 4

        if wp_type == 'Summary task':
            match = re.match(r'\[(E\d+)\]', subject)
            if match:
                epic_code = match.group(1)
                title = re.sub(r'\[E\d+\]\s*', '', subject)
                epics[epic_code] = {
                    'id': wp_id,
                    'code': epic_code,
                    'title': title,
                    'description': desc_raw,
                    'priority': priority,
                    'status': status,
                    'tasks': []
                }
        elif wp_type == 'Task':
            match = re.match(r'\[(E\d+-\d+)\]', subject)
            if match:
                task_code = match.group(1)
                epic_code = task_code.split('-')[0]
                title = re.sub(r'\[E\d+-\d+\]\s*', '', subject)
                tasks[task_code] = {
                    'id': wp_id,
                    'code': task_code,
                    'title': title,
                    'description': desc_raw,
                    'epic': epic_code,
                    'priority': priority,
                    'status': status,
                    'hours': hours
                }

    # Link tasks to epics
    for task_code, task in sorted(tasks.items()):
        epic_code = task['epic']
        if epic_code in epics:
            epics[epic_code]['tasks'].append(task)

    # Create feature files
    script_dir = os.path.dirname(os.path.abspath(__file__))
    features_dir = os.path.join(os.path.dirname(script_dir), 'features')
    os.makedirs(features_dir, exist_ok=True)

    # Delete old files (except template)
    for f in os.listdir(features_dir):
        if f.endswith('.yaml') and f != '_template.yaml':
            os.remove(os.path.join(features_dir, f))

    today = datetime.now().strftime('%Y-%m-%d %H:%M')

    print(f"\n[2/3] Generation de {len(epics)} fichiers features...")

    for code in sorted(epics.keys(), key=lambda x: int(x[1:])):
        epic = epics[code]
        slug = re.sub(r'[^a-z0-9]+', '-', epic['title'].lower()).strip('-')[:40]
        filename = f"{code.lower()}-{slug}.yaml"
        filepath = os.path.join(features_dir, filename)

        total_hours = sum(t['hours'] for t in epic['tasks'])
        days = max(1, total_hours // 8)

        # Determine module from title
        module = 'backend'
        if 'frontend' in epic['title'].lower():
            module = 'frontend'
        elif 'storefront' in epic['title'].lower():
            module = 'storefront'

        # Escape description for YAML
        desc = epic['description'][:500] if epic['description'] else f"Feature {code} - {epic['title']}"
        desc = desc.replace('\n', '\n    ')

        content = f'''# ═══════════════════════════════════════════════════════════════════
# FEATURE {code}: {epic['title']}
# ═══════════════════════════════════════════════════════════════════
# Synchronisé depuis OpenProject le {today}
# Tickets: {len(epic['tasks'])} | Estimation: {total_hours}h (~{days} jours)

feature:
  id: "{code}"
  openproject_id: {epic['id']}
  name: "{epic['title']}"
  description: |
    {desc}

  module: "{module}"
  priority: "{epic['priority']}"
  estimated_days: {days}
  status: "todo"

# ═══════════════════════════════════════════════════════════════════
# TICKETS INCLUS
# ═══════════════════════════════════════════════════════════════════

tickets:
'''

        for task in epic['tasks']:
            task_desc = task['description'][:200] if task['description'] else 'TODO'
            task_desc = task_desc.replace('\n', ' ').replace('"', "'")
            content += f'''
  - id: "{task['code']}"
    openproject_id: {task['id']}
    title: "{task['title']}"
    priority: "{task['priority']}"
    estimated_hours: {task['hours']}
    status: "{task['status']}"
'''

        content += f'''
# ═══════════════════════════════════════════════════════════════════
# NOTES
# ═══════════════════════════════════════════════════════════════════

notes: |
  Importé depuis OpenProject le {today}
  URL: https://aam.openproject.com/work_packages/{epic['id']}
'''

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        print(f"   + {filename} ({len(epic['tasks'])} tasks)")

    print(f"\n[DONE] {len(epics)} features generees dans {features_dir}")

if __name__ == "__main__":
    main()
