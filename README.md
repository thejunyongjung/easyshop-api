# 🛒 EasyShop — E-Commerce REST API

> The backend "engine" behind an online store. It lets shoppers browse a catalog,
> sign in, fill a shopping cart, manage their profile, and check out — and lets
> store admins manage the products and categories. Built with **Spring Boot** and **MySQL**.

`Java 17` · `Spring Boot 4` · `Spring Security (JWT)` · `Spring Data JPA` · `MySQL` · `JUnit 5`

---

## 🎥 Demo

<!--
  Add your demo video here (see the README guide). Easiest options:
  - YouTube (Unlisted): paste a clickable thumbnail or link, e.g.
        [▶ Watch the 4-minute demo](https://youtu.be/YOUR_VIDEO_ID)
  - Short clip (<= 10 MB): edit this README on github.com and drag the .mp4/.mov into the editor.
-->
▶ **Demo video:** _(add your link here)_

---

## 📖 What is EasyShop? (the plain-English version)

EasyShop is the **behind-the-scenes program** that powers an online store's website.
When a customer clicks "Add to Cart" on the site, the website quietly asks *this* program
to do the work — find the product, remember the cart, place the order — and save it all
to a database. This kind of program is called a **REST API**.

There are two kinds of people who use it:

- 🛍️ **Shoppers** — browse and search products, create an account, add things to a cart, and check out.
- 🛠️ **Admins** — add, edit, and remove products and categories in the store.

## 🛍️ What you can do

**As a shopper**
- Browse the full product catalog and filter by category, price, or color
- Register an account and log in (securely, with a token)
- Add items to your cart, change quantities, or empty it
- View and update your profile
- Check out — turn your cart into an order

**As an admin**
- Create, edit, and delete products and categories
- (Regular shoppers are blocked from these — only admins can manage the catalog)

> 📸 _Add screenshots here so reviewers can see it instantly:_
> `![Storefront](images/storefront.png)` and `![Cart](images/cart.png)`
> _(Create an `images/` folder, drop your screenshots in, and update the names.)_

## 🏗️ How it's built (in three layers)

The app is organized into three simple layers — each with one job:

```
   Controller   →   handles web requests (URLs, status codes, who's allowed in)
   Service      →   the "brain": business logic
   Repository   →   talks to the MySQL database
```

So a request flows **Controller → Service → Repository → Database**, and the answer flows back.
This keeps each part small and easy to test.

## ✨ What I built and fixed

**🐛 Fixed two hidden bugs (with tests):**
1. **Search was hiding most products.** A stray filter only let "featured" items through, so the
   storefront showed a fraction of the catalog. Removed it — now search returns everything that matches.
2. **Product edits weren't fully saving.** Editing a product returned "OK" but the **stock** never
   changed. Fixed so every field saves.

**🚀 Built new features:**
- **Categories** — full create / read / update / delete (admin-only writes)
- **Shopping cart** — add (or increase quantity), update, and clear
- **User profile** — view and update your own info
- **Checkout** — convert a cart into a saved order

**🛡️ Added input validation + clean errors (bonus):**
- Bad input (blank name, negative price…) is rejected with a clear `400` message saying exactly
  what's wrong — instead of saving junk or crashing.

---

## 🚀 Getting started

**You'll need:** JDK 17+ and a running MySQL server.

1. **Create the database** (in MySQL Workbench, or the terminal):
   ```bash
   mysql -u root -p < database/create_database_easyshop.sql
   ```
   This builds the `easyshop` database, its tables, and sample data.
2. **Tell the app your MySQL login** via environment variables (kept out of the code):
   - `DB_USERNAME` — e.g. `root`
   - `DB_PASSWORD` — your MySQL password
3. **Start the API:**
   ```bash
   ./mvnw spring-boot:run
   ```
   It runs at `http://localhost:8080`.

**Sample logins** (password is `password` for all): `user` (shopper), `admin` (admin).

## 🔐 Authentication
- `POST /register` → `{ "username", "password", "confirmPassword" }`
- `POST /login` → `{ "username", "password" }` → returns a **JWT token**
- Send the token on protected requests: `Authorization: Bearer <token>`

## 📡 API reference

### Categories
| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/categories` | public | all categories |
| GET | `/categories/{id}` | public | one category (404 if missing) |
| GET | `/categories/{id}/products` | public | products in a category |
| POST | `/categories` | ADMIN | create → 201 |
| PUT | `/categories/{id}` | ADMIN | update |
| DELETE | `/categories/{id}` | ADMIN | delete → 204 |

### Products
| Method | Path | Access | Notes |
|---|---|---|---|
| GET | `/products` | public | search params: `cat`, `minPrice`, `maxPrice`, `subCategory` |
| GET | `/products/{id}` | public | one product (404 if missing) |
| POST | `/products` | ADMIN | create → 201 |
| PUT | `/products/{id}` | ADMIN | update |
| DELETE | `/products/{id}` | ADMIN | delete → 204 |

### Cart · Profile · Orders (all require login)
| Method | Path | Notes |
|---|---|---|
| GET | `/cart` | the current user's cart |
| POST | `/cart/products/{id}` | add (qty 1; +1 if already in cart) → 201 |
| PUT | `/cart/products/{id}` | set quantity — body `{ "quantity": n }` |
| DELETE | `/cart` | empty the cart |
| GET | `/profile` | the current user's profile |
| PUT | `/profile` | update the profile |
| POST | `/orders` | checkout — cart → order, then empties the cart → 201 |

## 🐛 Bug fixes (detail)
1. **Search left products out** — `ProductService.search` ended with `.filter(Product::isFeatured)`, silently dropping every non-featured product. Removed it.
2. **Edits dropped `stock`** — `ProductService.update` copied every field except `stock`. Added `existing.setStock(product.getStock())`.

Both are locked in by unit tests in `ProductServiceTest`.

## 🛡️ Validation & error handling (bonus)
- Bean Validation on `Product` / `Category` (`@NotBlank`, `@Positive`, `@PositiveOrZero`) with `@Valid` on POST/PUT bodies.
- A global `@RestControllerAdvice` (`GlobalExceptionHandler`) turns validation failures into a `400` with a `{ field: message }` body, and `ResponseStatusException`s into a consistent `{ status, message }` — no stack traces exposed.

## 🧪 Testing
```bash
./mvnw test
```
- **16 tests** using `@DataJpaTest` + an in-memory **H2** database seeded by `src/test/resources/test-insert-data.sql` — fast and isolated from real MySQL.
- Covers: both bug fixes; full `CategoryService` CRUD; `ShoppingCartService` (add / increment / update); `ProfileService` (get / update); `OrderService` checkout (incl. empty-cart rejection).
- The web / security / validation layer is exercised through the bundled Insomnia collection.

## 📂 Project structure
```
src/main/java/org/yearup
├── controllers   # REST endpoints (Products, Categories, ShoppingCart, Profile, Orders, ...)
├── models        # JPA entities + response models
├── repository    # Spring Data JPA repositories
├── service       # business logic
└── security      # JWT authentication & authorization
```
