# 🛒 EasyShop — E-Commerce REST API

> The backend "engine" behind an online store. It lets shoppers browse a catalog,
> sign in, fill a shopping cart, manage their profile, and check out — and lets
> store admins manage the products and categories. Built with **Spring Boot** and **MySQL**.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-59666C?logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-16%20passing-25A162?logo=junit5&logoColor=white)

![EasyShop storefront](images/Main_Before_Login.png)

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

Add items to the cart (here: 2 phones + 1 laptop), then check out:

![Cart with items and checkout](images/Cart_After_Adding_Items.png)

View and update your profile:

![Updating the user profile](images/Saving_User_Profile.png)

**As an admin**
- Create, edit, and delete products and categories
- Regular shoppers are blocked from these — only admins can manage the catalog

![Logged in as an admin](images/Main_Admin_login.png)

## 🏗️ How it's built (in three layers)

The app is organized into three simple layers — each with one job. A request flows down and
the answer flows back, which keeps each part small and easy to test:

```mermaid
flowchart TD
    A["🌐 Controller<br/>@RestController — URLs, status codes, security"] --> B["🧠 Service<br/>@Service — business logic"]
    B --> C["🗄️ Repository<br/>JpaRepository — data access"]
    C --> D[("MySQL Database")]
    style A fill:#c7d7f5,stroke:#3b5bdb,color:#111
    style B fill:#c3f0d8,stroke:#2f9e44,color:#111
    style C fill:#cfe8ff,stroke:#1971c2,color:#111
    style D fill:#ffe8cc,stroke:#e8590c,color:#111
```

## 🗄️ Database

Seven related tables back the store. `users` owns `profiles`, `orders`, and `shopping_cart`;
`products` belong to a `category`; and an order is one `orders` row plus one `order_line_items`
row per product. (Diagram reverse-engineered from the actual `easyshop` schema.)

![EasyShop database ER diagram](images/Database_ERD.png)

## ✨ What's implemented

This is a final-exam capstone: fix the existing bugs, then build the new features. Everything below
is done — **all required work, every optional phase, and the bonus.**

### ✅ Required
- **Phase 1 — Categories (CRUD).** Full `GET / POST / PUT / DELETE` on `/categories` via
  `CategoriesController` + `CategoryService`. Writes are **admin-only**; reads are public.
- **Phase 2 — Bug fixes (with unit tests).**
  - **Bug 1 — search hid products.** A leftover `.filter(Product::isFeatured)` dropped every
    non-featured product; removed it so search returns the full catalog.
  - **Bug 2 — edits dropped `stock`.** `update` copied every field except `stock`; added
    `existing.setStock(...)` so a product fully updates.

### 🎁 Optional (all completed)
- **Phase 3 — Shopping cart.** `GET /cart`, `POST /cart/products/{id}` (insert, or +1 if the product
  is already in the cart), `PUT /cart/products/{id}` (set quantity — *bonus*), `DELETE /cart` (clear).
- **Phase 4 — User profile.** `GET /profile` and `PUT /profile` for the logged-in user.
- **Phase 5 — Checkout.** `POST /orders` turns the cart into a saved order (an order header plus one
  line item per product) and empties the cart. New `Order` / `OrderLineItem` models, repositories,
  service, and controller.

### 🏆 Bonus (beyond the spec)
- **Input validation** + a **global error handler** for clean, consistent responses — see the
  **Validation & error handling** section below.

## 💡 An interesting piece of code — `OrderService.checkout`

Turning a cart into an order means **several database writes**: insert the order, insert one
line item per product, then empty the cart. If one failed halfway, you'd get a broken order.
Two things make this method interesting:

**1. It's all-or-nothing (`@Transactional`).** The whole method runs in one transaction —
if anything throws, every write rolls back, so there are never half-finished orders.

**2. Save the order first to get its database-generated id.** The line items need the
`order_id`, but that id doesn't exist until the order row is inserted. So I save the order
first, read back the id the database assigned, and attach it to each line item.

```java
@Transactional
public Order checkout(int userId)
{
    ShoppingCart cart = shoppingCartService.getByUserId(userId);

    if (cart.getItems().isEmpty())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart.");

    // save the order header first -> the DB assigns its id
    Order savedOrder = orderRepository.save(order);   // before: id = 0, after: id = DB value

    // one line item per product, linked by that new id
    for (ShoppingCartItem item : cart.getItems().values())
    {
        OrderLineItem lineItem = new OrderLineItem();
        lineItem.setOrderId(savedOrder.getOrderId());
        lineItem.setProductId(item.getProductId());
        lineItem.setSalesPrice(item.getProduct().getPrice());
        lineItem.setQuantity(item.getQuantity());
        orderLineItemRepository.save(lineItem);
    }

    shoppingCartService.clear(userId);   // empty the cart
    return savedOrder;
}
```

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

> [!TIP]
> **Sample logins** (password is `password` for all): `user` (shopper) and `admin` (admin).

## 🔐 Authentication
- `POST /register` → `{ "username", "password", "confirmPassword" }`
- `POST /login` → `{ "username", "password" }` → returns a **JWT token**
- Send the token on protected requests: `Authorization: Bearer <token>`

![Login screen](images/User_Login_Page.png)

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

**Input validation** — Bean Validation on the `Product` and `Category` models (`@NotBlank`,
`@Positive`, `@PositiveOrZero`) with `@Valid` on the POST/PUT bodies, so bad input is rejected
*before* it ever reaches the database (no more saving junk or crashing).

**Global error handler** — a single `@RestControllerAdvice` (`GlobalExceptionHandler`) turns
exceptions into clean, consistent JSON instead of raw error pages:

| Situation | Response |
|---|---|
| Invalid body — `@Valid` fails | `400` with `{ field: message }` for each bad field (e.g. `{ "name": "must not be blank" }`) |
| Not found / bad request — `ResponseStatusException` | `{ status, message }` (e.g. `404` `"Category not found"`, or `400` `"Cannot checkout an empty cart."`) |
| Non-admin hits an admin-only endpoint — `AuthorizationDeniedException` | `403` `{ status, error, message }` |

Stack traces are switched off (`spring.web.error.include-stacktrace=never`), so no response ever
leaks internal class names or framework details — cleaner for users and safer.

## 🧪 Testing
```bash
./mvnw test
```
**16 unit tests, all green.** They use `@DataJpaTest` with an in-memory **H2** database seeded by
`src/test/resources/test-insert-data.sql`, so they run fast and never touch the real MySQL data.

What each test class verifies — and why:

| Test class | # | What it proves |
|---|---|---|
| `ProductServiceTest` | 2 | **Regression guards for both bugs** — search returns *all* products (Bug 1), and `update` persists `stock` (Bug 2). |
| `CategoryServiceTest` | 5 | Full CRUD — get all, get-missing returns `null`, create adds, update persists changes, delete removes. |
| `ShoppingCartServiceTest` | 3 | New product starts at quantity 1; adding the same product increments it; update sets the exact quantity. |
| `ProfileServiceTest` | 3 | Get a profile, get-missing returns `null`, and update persists the changed fields. |
| `OrderServiceTest` | 2 | Checkout creates the order **and** empties the cart; checking out an empty cart throws `400`. |
| `ProductRepositoryTest` | 1 | `findById` returns the correct product (repository wiring works). |

The two `ProductServiceTest` cases matter most: they **lock in the bug fixes** so neither bug can
silently return. Test names follow `subject_shouldVerb_object`, and each has clear arrange / act /
assert steps. The web, security, and validation layers are exercised end-to-end through the bundled
**Insomnia** collection.

## 🔮 Future versions

Features I'd build next, ranked by value vs. effort:

1. **Order history ("My Orders")** — view past orders. Builds directly on checkout:
   add `findByUserId` to `OrderRepository`, a `GET /orders` endpoint, and an orders page on the site.
2. **Stock enforcement** — decrement `stock` when an order is placed and block adding
   out-of-stock items (stock checks inside `OrderService.checkout` and `ShoppingCartService`).
3. **Product reviews & ratings** — a `reviews` table, a `ReviewController`/`ReviewService`,
   and an average-rating field on the product response.
4. **Payment processing** — a payment step at checkout (e.g., Stripe) and an order
   `status` column (PENDING → PAID → SHIPPED).
5. **Search paging & sorting** — return products in pages, sortable by price/name,
   using Spring Data `Pageable`.
6. **Wishlist / favorites** — a `wishlist` table and endpoints to save items for later.

## 📂 Project structure
```
src/main/java/org/yearup
├── controllers   # REST endpoints (Products, Categories, ShoppingCart, Profile, Orders, ...)
├── models        # JPA entities + response models
├── repository    # Spring Data JPA repositories
├── service       # business logic
└── security      # JWT authentication & authorization
```
