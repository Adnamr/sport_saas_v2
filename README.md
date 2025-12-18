# 🏀 Sport SaaS - Gestion de Projet

## 🚀 Commandes pour travailler sur les Features

### Démarrer une feature
```bash
make feature FEATURE=e1-module-configuration
```

**Ce que ça fait :**
1. ✅ `git pull origin develop`
2. ✅ Crée branche `feature/e1-module-configuration`
3. ✅ OpenProject: Feature E1 + tous ses tickets → **In Progress**
4. ✅ Génère le contexte pour Claude

### Terminer une feature
```bash
make feature-done
```

**Ce que ça fait :**
1. ✅ Vérifie que le code compile
2. ✅ Lance les tests
3. ✅ `git add -A && git commit`
4. ✅ `git push -u origin feature/xxx`
5. ✅ OpenProject: Feature + tickets → **In Review**
6. ✅ Affiche le lien pour créer la PR

### Après merge de la PR
```bash
make op-close-feature FEATURE=E1
```

**Ce que ça fait :**
1. ✅ OpenProject: Feature + tickets → **Done**

---

## 📋 Liste des Features (E1-E26)

### Backend (E1-E10)
| ID | Feature | Commande |
|----|---------|----------|
| E1 | Configuration Spring Boot | `make feature FEATURE=e1-module-configuration` |
| E2 | Multi-tenant | `make feature FEATURE=e2-module-tenant` |
| E3 | Authentification JWT | `make feature FEATURE=e3-module-endpoint` |
| E4 | Catalogue | `make feature FEATURE=e4-module-category` |
| E5 | Inventaire | `make feature FEATURE=e5-module-stock` |
| E6 | Commandes | `make feature FEATURE=e6-module-cart` |
| E7 | Facturation | `make feature FEATURE=e7-module-invoice` |
| E8 | Gestion Users | `make feature FEATURE=e8-module-users` |
| E9 | Email Service | `make feature FEATURE=e9-service-emailservice` |
| E10 | Dashboard API | `make feature FEATURE=e10-module-dashboard-api` |

### Frontend Admin (E11-E19)
| ID | Feature | Commande |
|----|---------|----------|
| E11 | Setup Angular | `make feature FEATURE=e11-module-frontend` |
| E12 | Docker & CI/CD | `make feature FEATURE=e12-module-dockerfile` |
| E13 | Module Auth | `make feature FEATURE=e13-module-frontend-auth` |
| E14 | Dashboard | `make feature FEATURE=e14-module-frontend-dashboard` |
| E15 | Catalogue | `make feature FEATURE=e15-module-frontend-catalog` |
| E16 | Inventaire | `make feature FEATURE=e16-module-frontend-inventory` |
| E17 | Commandes | `make feature FEATURE=e17-module-frontend-orders` |
| E18 | Facturation | `make feature FEATURE=e18-module-frontend-billing` |
| E19 | Paramètres | `make feature FEATURE=e19-module-frontend-settings` |

### Storefront (E20-E23)
| ID | Feature | Commande |
|----|---------|----------|
| E20 | Setup & Layout | `make feature FEATURE=e20-module-storefront-setup` |
| E21 | Catalogue Public | `make feature FEATURE=e21-module-storefront-catalog` |
| E22 | Panier & Checkout | `make feature FEATURE=e22-module-storefront-checkout` |
| E23 | Espace Client | `make feature FEATURE=e23-module-storefront-account` |

### API Complémentaires (E24-E26)
| ID | Feature | Commande |
|----|---------|----------|
| E24 | API Publique | `make feature FEATURE=e24-module-api-publique` |
| E25 | Settings API | `make feature FEATURE=e25-module-settings-api` |
| E26 | Compléments | `make feature FEATURE=e26-module-complements` |

---

## ⚙️ Configuration OpenProject

Édite `team/config.yaml` :
```yaml
openproject:
  url: "https://aam.openproject.com"
  token: "VOTRE_TOKEN_API"
  project: "saas-sport-v1"
```

---

## 🔄 Workflow Complet

```bash
# 1. DÉMARRER
make feature FEATURE=e1-module-configuration

# 2. CODER (avec Claude)
make claude
# Copier .claude-feature-context.md dans Claude

# 3. TERMINER
make feature-done

# 4. CRÉER PR sur GitHub (lien affiché)

# 5. APRÈS MERGE
make op-close-feature FEATURE=E1

# 6. PROCHAINE FEATURE
make feature FEATURE=e2-module-tenant
```
