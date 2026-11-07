# SmartLocal

**AI-Based Hyperlocal Product Discovery and Inventory Management System for Local Retail Stores**

SmartLocal connects neighborhood retail stores directly with nearby customers. Customers can discover local shops in their vicinity, search for real-time in-store products, check live shelf availability, compare multi-store prices, and contact retailers directly via phone or WhatsApp.

---

## 📌 Technology Stack

* **Backend Framework:** Java 21 / 25 LTS, Spring Boot 3.3.5
* **Security & Authentication:** Spring Security 6 with BCrypt Password Hashing, Session Management, and Role-Based Access Control (RBAC)
* **Persistence & ORM:** Spring Data JPA, Hibernate 6.x
* **Database & Storage:**
  * **Default (Zero-Configuration):** File-based persistent H2 Database (`./data/smartlocal_db.mv.db`)
  * **Production Profile:** MySQL 8.x database configuration (`application-mysql.properties`)
* **Presentation & UI Engine:** Thymeleaf 3 template engine + Thymeleaf Extras Spring Security 6
* **Frontend Design System:** Modern custom CSS3 design system (`style.css`), Bootstrap 5.3, Bootstrap Icons 1.11.3, Google Fonts (*Inter*)
* **Build Tool:** Apache Maven 3.x
* **Testing & Verification:** Python 3 HTTP test harness & browser verification scripts

---

## 📂 Project Structure

```text
SmartLocal_Shop/
├── data/                                      # Persistent H2 database storage
│   └── smartlocal_db.mv.db
├── src/
│   ├── main/
│   │   ├── java/com/smartlocal/
│   │   │   ├── SmartLocalApplication.java      # Spring Boot Main Entry Point
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java              # BCryptPasswordEncoder & general beans
│   │   │   │   ├── SecurityConfig.java         # Spring Security 6 & RBAC authorization rules
│   │   │   │   └── CustomAuthenticationSuccessHandler.java # Smart role-based dashboard router
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java         # Public landing page & featured showcase
│   │   │   │   ├── AuthController.java         # Login, registration & access-denied handlers
│   │   │   │   ├── DashboardController.java    # Customer dashboard & location switcher
│   │   │   │   ├── RetailerDashboardController.java # Merchant profile & shop registration
│   │   │   │   ├── RetailerInventoryController.java # Inventory CRUD, prices & stock alerts
│   │   │   │   ├── CustomerProductController.java   # Product discovery & multi-store price radar
│   │   │   │   ├── RetailerContactController.java   # Dedicated shop contact & fallback pages
│   │   │   │   └── AdminController.java        # Protected admin console & governance
│   │   │   ├── dto/
│   │   │   │   ├── UserRegistrationDto.java    # Validated registration payload
│   │   │   │   ├── DemoLocationDto.java        # Demonstration location information
│   │   │   │   ├── ShopSummaryDto.java         # Shop listing card data transfer object
│   │   │   │   ├── ShopRegistrationDto.java   # Retailer shop registration form object
│   │   │   │   ├── ProductDto.java             # Retailer inventory item form & validation
│   │   │   │   ├── ProductSearchResultDto.java # Customer search result card with stock status
│   │   │   │   ├── ProductPriceComparisonDto.java # Multi-store price comparison radar data
│   │   │   │   └── StorePriceQuoteDto.java     # Individual shop price, stock & contact quote
│   │   │   ├── entity/
│   │   │   │   ├── Role.java                   # Enum: ROLE_CUSTOMER, ROLE_RETAILER, ROLE_ADMIN
│   │   │   │   ├── User.java                   # JPA User entity with BCrypt password & role
│   │   │   │   ├── Shop.java                   # Shop entity linked 1-to-1 with retailer
│   │   │   │   ├── Product.java                # Inventory item linked to shop with price & stock
│   │   │   │   └── ApprovalStatus.java         # Enum: APPROVED, PENDING, REJECTED
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java         # User entity database access
│   │   │   │   ├── ShopRepository.java         # Shop entity database access
│   │   │   │   └── ProductRepository.java      # Product entity database access
│   │   │   └── service/
│   │   │       ├── UserService.java            # Account registration & Spring UserDetailsService
│   │   │       ├── ShopDiscoveryService.java   # Location filtering & demo shop showcase
│   │   │       ├── ShopService.java            # Retailer shop profile & ownership management
│   │   │       ├── ProductService.java         # Inventory CRUD & low-stock calculations
│   │   │       ├── CustomerProductService.java # Cross-store search & price comparison engine
│   │   │       ├── SearchSynonymService.java   # Rule-based synonym expansion dictionary
│   │   │       └── impl/
│   │   │           ├── UserServiceImpl.java
│   │   │           ├── ShopDiscoveryServiceImpl.java
│   │   │           ├── ShopServiceImpl.java
│   │   │           ├── ProductServiceImpl.java
│   │   │           ├── CustomerProductServiceImpl.java
│   │   │           └── SearchSynonymServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties          # Active configuration (H2 persistent file default)
│   │       ├── application-h2.properties       # H2 database configuration
│   │       ├── application-mysql.properties    # MySQL database configuration
│   │       ├── static/
│   │       │   ├── css/style.css               # Clean modern custom CSS & responsive styling
│   │       │   └── js/main.js                  # Frontend utilities & contact handlers
│   │       └── templates/
│   │           ├── index.html                  # Public homepage with nearby shop showcase
│   │           ├── login.html                  # Login page with 1-click demo role tabs
│   │           ├── register.html               # Registration with Customer & Retailer roles
│   │           ├── access-denied.html          # 403 Forbidden access-denied page
│   │           ├── customer/
│   │           │   ├── compare-products.html   # Full-page multi-store price comparison view
│   │           │   ├── shop-details.html       # Individual store profile & catalog view
│   │           │   └── contact-retailer.html   # Standalone contact retailer page
│   │           ├── dashboard/
│   │           │   ├── customer-dashboard.html # Customer dashboard (locations, search, radar)
│   │           │   ├── retailer-dashboard.html # Retailer dashboard (store profile & setup)
│   │           │   ├── retailer-inventory.html # Retailer inventory console (CRUD, stock & prices)
│   │           │   └── admin-dashboard.html    # Administrator dashboard (users, shops, approvals)
│   │           └── fragments/
│   │               ├── navbar.html             # Role-aware sticky top navigation bar
│   │               ├── footer.html             # Footer with 10-module progress indicator
│   │               └── contact-modal.html      # Reusable contact modal (Call & WhatsApp)
│   └── test/
└── pom.xml                                     # Maven project build descriptor
```

---

## 🗄️ Database Configuration

### 1. Default Setup: Persistent H2 Database (Zero Setup Needed)
The project is pre-configured to use a file-backed H2 relational database in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/smartlocal_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```
* **Persistent Storage:** Data is stored on disk in `./data/smartlocal_db.mv.db`, ensuring all changes survive application restarts.
* **Idempotent Seeding:** On startup, the system uses idempotent checks (`existsByEmail`, `existsByShopAndProductName`, etc.) so that demo accounts, shops, and inventory records are **never duplicated** when the server restarts.
* **H2 Console:** Accessible locally at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/smartlocal_db`, User: `sa`, Password: *blank*).

### 2. Optional Setup: MySQL 8.x Database
To run against a local or remote MySQL instance, activate the `mysql` Spring profile:
```properties
# Add or set in application.properties:
spring.profiles.active=mysql

# Configure credentials in application-mysql.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/smartlocal_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## 🚀 Application Startup Instructions

### Prerequisites
* **Java Development Kit (JDK):** Version 17, 21, or 25 installed and available on PATH (`java -version`).
* **Apache Maven:** Version 3.8+ installed (`mvn -v`) or use the Maven wrapper.

### Build & Run
1. Open a terminal / command prompt in the project root directory:
   ```bash
   cd d:/Projects/SmartLocal_Shop
   ```
2. Compile and package the project:
   ```bash
   mvn clean compile
   ```
3. Start the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
4. Wait until the terminal outputs:
   ```text
   Started SmartLocalApplication in X.XXX seconds (process running...)
   ```

### Local Application URL
Open any web browser and navigate to:
👉 **`http://localhost:8080`**

---

## 🔑 Demonstration Accounts & Credentials

The application automatically seeds demonstration accounts for all system roles. On the login page (`/login`), convenient **One-Click Fill** tabs are available to immediately populate credentials.

| Role | Name | Email Address | Password | Associated Store / Privileges | Expected Dashboard |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | Demo Administrator | `admin@smartlocal.com` | `Admin@123` | System Governance, Approvals, Users | `/admin/dashboard` |
| **Customer 1** | Rahul Sharma | `customer@smartlocal.com` | `Customer@123` | Active Shopper | `/customer/dashboard` |
| **Customer 2** | Priya Deshmukh | `priya.customer@smartlocal.com` | `Customer@123` | Active Shopper | `/customer/dashboard` |
| **Customer 3** | Amit Verma | `amit.customer@smartlocal.com` | `Customer@123` | Active Shopper | `/customer/dashboard` |
| **Retailer 1** | Vikram Malhotra | `retailer@smartlocal.com` | `Retailer@123` | Fresh Mart (Location A) | `/retailer/dashboard` |
| **Retailer 2** | Sunita Agarwal | `dailyneeds@smartlocal.com` | `Retailer@123` | Daily Needs Store (Location A) | `/retailer/dashboard` |
| **Retailer 3** | Rajesh Kulkarni | `greenbasket@smartlocal.com` | `Retailer@123` | Green Basket (Location A) | `/retailer/dashboard` |
| **Retailer 4** | Mahesh Patil | `citydairy@smartlocal.com` | `Retailer@123` | City Dairy (Location B) | `/retailer/dashboard` |
| **Retailer 5** | Sneha Joshi | `smartstore@smartlocal.com` | `Retailer@123` | Smart General Store (Location B) | `/retailer/dashboard` |
| **Retailer 6** | Anand Verma | `anand.textiles@smartlocal.com` | `Retailer@123` | Anand Handloom *(Pending Approval Demo)* | `/retailer/dashboard` |

---

## 🖥️ Expected Dashboard for Each Demo Account

### 1. Customer Accounts (`ROLE_CUSTOMER`)
* **URL:** `/customer/dashboard`
* **Features:**
  * Personalized customer greeting and profile card.
  * **Location Switcher:** Toggle between Central Pune, East Pune, All Pune, or acquire Real-Time Live GPS.
  * **Nearby Shops Section:** Displays neighborhood stores registered in the active location.
  * **AI-Assisted Product Search:** Search by product title, brand, or everyday synonyms (e.g. *chawal*, *doodh*, *charger*).
  * **Real-Time Stock Badges:** Shows live inventory quantities with color-coded status (`In Stock`, `Low Stock`, `Out of Stock`).
  * **Multi-Store Price Comparison Radar:** Displays comparative quotes across stores with a **Lowest Price** winner highlight (excluding out-of-stock items).
  * **Contact Retailer Actions:** Direct telephone dialer link (`tel:`) and WhatsApp click-to-chat link with prepopulated product context.

### 2. Retailer Accounts (`ROLE_RETAILER`)
* **URL:** `/retailer/dashboard` and `/retailer/inventory`
* **Features:**
  * **Shop Profile Console:** Displays store name, store address, location zone, business category, operating hours, and approval status badge.
  * **Inventory Management Console (`/retailer/inventory`):**
    * Add new inventory items with non-negative price and quantity validation.
    * Edit existing product details (pricing, stock levels, low-stock threshold).
    * Delete items from the store catalog.
    * Filter inventory by status: *All*, *In Stock*, *Low Stock Alerts*, and *Out of Stock*.
    * Strict backend isolation: retailers can only ever view and edit their own shop's inventory.

### 3. Administrator Account (`ROLE_ADMIN`)
* **URL:** `/admin/dashboard`
* **Features:**
  * **KPI Metrics Radar:** Total system users (Customers, Retailers, Admins), registered shops, active catalog SKUs, and service locations.
  * **Shop Approval Workflow:** Inspect pending retailer store registrations and **Approve** or **Reject** with one click.
  * **Operational Status Control:** Instantly **Activate** or **Deactivate** stores to show/hide them in customer search results.
  * **User Directory & Account Status:** View all registered accounts and enable/disable access.
  * **Global Catalog Inspection:** Audit product inventory across all retail stores simultaneously.

---

## 📍 Local Service Zones & Real-Time Geolocation Architecture

The platform supports hyperlocal store discovery through real Pune neighborhood zones alongside dynamic **HTML5 Real-Time GPS** and Haversine formula proximity calculation:

### 1. Zone A: Central Pune (`LOC-PUNE-A`)
* **Service Area:** Central Pune (F.C. Road / Deccan)
* **Landmark:** Near F.C. Road Arcade & Deccan Gymkhana
* **Coordinates:** `18.5204° N, 73.8567° E`
* **Characteristics:** High-density commercial and academic zone with traditional grocery, organic produce, and stationery merchants.
* **Stores in Central Pune:**
  1. **Fresh Mart:** Groceries, staples, cables, and packaged essentials.
  2. **Daily Needs Store:** Household goods, wheat flour, basmati rice, and computer peripherals.
  3. **Green Basket:** Farm-fresh organic eggs, olive oil, and organic grains.
  4. **Heritage Book & Stationery Depot:** Traditional bookstore demonstrating graceful missing-phone fallback handling.

### 2. Zone B: East Pune (`LOC-PUNE-B`)
* **Service Area:** East Pune (Viman Nagar / Nagar Road)
* **Landmark:** Nagar Road Plaza & Viman Nagar Boulevard
* **Coordinates:** `18.5580° N, 73.9140° E`
* **Characteristics:** Residential and tech hub with dairy boutiques, consumer electronics, and healthcare supplies.
* **Stores in East Pune:**
  1. **City Dairy:** Fresh toned milk, pure buffalo desi ghee, paneer, and filter coffee.
  2. **Smart General Store:** Dairy essentials, fast-charging cables, healthcare tablets, and infrared thermometers.

### 3. Real-Time GPS Geolocation & Haversine Distance Engine
* **Browser Geolocation API (`navigator.geolocation`):** Allows customers to click **Live GPS** on their device to detect current latitude and longitude.
* **Dynamic Proximity Sorting:** Automatically resolves physical distance in kilometers using the Haversine formula and sorts shops in ascending order of distance.
* **REST API:** Client-side asynchronous proximity endpoint at `GET /api/location/nearest?lat=...&lng=...`.

---

## 🏪 Demonstration Inventory & Multi-Store Pricing Matrix

The seeded inventory contains products demonstrating price variations, stock states, and search scenarios:

| Product Name | Category | Shop 1: Fresh Mart (Loc A) | Shop 2: Daily Needs (Loc A) | Shop 3: Green Basket (Loc A) | Shop 4: City Dairy (Loc B) | Shop 5: Smart Store (Loc B) |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **Aashirvaad Atta (5kg)** | Groceries | ₹265 (24 in stock) | **₹245 (35 in stock - Lowest)** | ₹279 (12 in stock) | &mdash; | &mdash; |
| **Fortune Basmati Rice (5kg)** | Groceries | **₹399 (20 in stock - Best Available)** | ₹425 (15 in stock) | ₹380 *(0 Out of Stock)* | &mdash; | &mdash; |
| **Pure Mustard Oil (1L)** | Groceries | ₹175 (18 in stock) | **₹169 (22 in stock - Lowest)** | &mdash; | &mdash; | &mdash; |
| **Farm Fresh Eggs (12pk)** | Organic | ₹125 *(3 Low Stock)* | &mdash; | **₹115 (25 in stock - Lowest)** | &mdash; | &mdash; |
| **USB-C Braided Cable (2m)** | Electronics | ₹269 (26 in stock) | ₹289 (14 in stock) | &mdash; | &mdash; | **₹249 (30 in stock - Lowest)** |
| **Wireless Optical Mouse** | Electronics | **₹849 (14 in stock - Lowest)** | ₹899 *(4 Low Stock)* | &mdash; | &mdash; | &mdash; |
| **Amul Toned Milk (1L)** | Dairy | &mdash; | &mdash; | &mdash; | **₹72 (45 in stock - Lowest)** | ₹75 (18 in stock) |
| **Pure Buffalo Desi Ghee (1L)**| Dairy | &mdash; | &mdash; | &mdash; | **₹620 (28 in stock - Lowest)** | ₹640 *(3 Low Stock)* |
| **Fresh Malai Paneer (500g)** | Dairy | &mdash; | &mdash; | &mdash; | **₹195 (20 in stock)** | ₹180 *(0 Out of Stock)* |

> **Key Rule-Enforcement Demonstration:** For *Fortune Basmati Rice*, Green Basket offers the nominal lowest price at ₹380, but its shelf stock is **0 (Out of Stock)**. The system **never recommends an out-of-stock item as the cheapest available option**; instead, Fresh Mart at ₹399 is correctly highlighted as the best available option!

---

## 🧪 Step-by-Step Demonstration Guide

Follow these steps to experience the complete end-to-end integration:

### Step 1: Administrator Governance & Store Approval
1. Navigate to `http://localhost:8080/login`.
2. Click the **Admin** pill on the demo panel and click **Fill** (`admin@smartlocal.com` / `Admin@123`), then click **Sign In**.
3. You are redirected to `/admin/dashboard`.
4. Observe the system metrics: registered users, active shops, and catalog products.
5. In the **Shop Approvals** table, locate the pending store: *"Anand Handloom & Organic Cottons"*.
6. Click **Approve** &mdash; notice the badge changes to a green `Approved` state.
7. Click **Deactivate** on any store to toggle its active status, then reactivate it.
8. Click **Logout** in the top navigation bar.

### Step 2: Retailer Inventory Management
1. Return to `http://localhost:8080/login`.
2. Click the **3 Retailers** tab, select **Fresh Mart** (`retailer@smartlocal.com` / `Retailer@123`), and sign in.
3. You land on the Retailer Dashboard (`/retailer/dashboard`). Click **Manage Inventory**.
4. In the inventory console (`/retailer/inventory`), view your store's live catalog.
5. Click **Add New Product**, fill in:
   * **Product Name:** *Organic Turmeric Powder (250g)*
   * **Category:** *Groceries & Daily Essentials*
   * **Selling Price:** `75.00`
   * **Stock Quantity:** `15`
   * **Low-Stock Alert Level:** `5`
6. Click **Save Product** &mdash; the new item appears instantly in your inventory.
7. Click **Edit** on any item to modify price or stock, or click **Filter: Low Stock** to view threshold warnings.
8. Click **Logout**.

### Step 3: Customer Location-Based Discovery (Central Pune)
1. Go to `/login`, select **3 Customers** tab, and choose **Rahul** (`customer@smartlocal.com` / `Customer@123`).
2. You land on `/customer/dashboard`.
3. Notice the **Active Zone: Central Pune (F.C. Road / Deccan)**.
4. Under **Nearby Retailers in this Zone**, observe the 3 active stores: *Fresh Mart*, *Daily Needs Store*, and *Green Basket*.
5. In the search bar, type:
   * `"atta"` or `"wheat"` &mdash; observe results showing Atta prices at Fresh Mart (₹265), Daily Needs (₹245 - marked Lowest Price), and Green Basket (₹279).
   * Notice the **AI-Assisted Search** banner explaining deterministic rule-based synonym expansion.
6. Search for `"rice"` or `"chawal"`:
   * Notice Green Basket shows ₹380 with an **Out of Stock** badge.
   * Notice Fresh Mart (₹399) is highlighted with the green **Lowest Available Price** trophy because the system does not recommend out-of-stock items as the best option.

### Step 4: Contacting the Retailer
1. On any product card (e.g. *USB-C Braided Cable* at Fresh Mart), click the **Contact** button.
2. The **Contact Retailer Modal** pops up with:
   * Store Name: *Fresh Mart*
   * Product: *Fast-Charging USB-C Braided Cable (2m) - ₹269*
   * Prepopulated Inquiry: `"Hello, I found your shop on SmartLocal. Is Fast-Charging USB-C Braided Cable (2m) currently available?"`
3. Click **Call Store** to open the telephone link (`tel:+919876543210`).
4. Click **Chat on WhatsApp** to preview the secure WhatsApp click-to-chat URL with encoded inquiry text.
5. Close the modal and locate *Heritage Book & Stationery Depot*:
   * Notice the phone call and WhatsApp buttons are gracefully **disabled** with an alert explaining no telephone is registered.

### Step 5: Switching to East Pune, All Pune, or Live GPS
1. In the top location switcher, click **East Pune** (or test **All Pune** or **Live GPS**).
2. Notice the page updates to display East Pune stores: *City Dairy* and *Smart General Store*.
3. Search for `"milk"` or `"doodh"`:
   * City Dairy offers Amul Milk at ₹72 (Lowest Price).
   * Smart General Store offers Amul Milk at ₹75.
4. Search for `"ghee"`:
   * City Dairy offers Desi Ghee at ₹620 (Lowest Price).
   * Smart General Store offers Desi Ghee at ₹640 (marked with an orange **Low Stock (3 left)** warning).
5. Click **Logout**.

---

## 🧭 10-Module Incremental Roadmap Status

1. ✅ **Module 1: Project Setup, Architecture & Public Landing Page**
2. ✅ **Module 2: Database-Backed Authentication, RBAC & Role Dashboards**
3. ✅ **Module 3: Authenticated Customer Dashboard, Local Product Discovery & Price Comparison**
4. ✅ **Module 4: Retailer Store Setup & Management**
5. ✅ **Module 5: Product Catalog & Store Inventory Management**
6. ✅ **Module 6: Hyperlocal Location Selection & Nearby Shop Discovery Engine**
7. ✅ **Module 7: In-Store Product Search & Real-Time Availability Engine**
8. ✅ **Module 8: Retailer Contact & Communication Workflow**
9. ✅ **Module 9: Protected Administrator Dashboard, Store Verification & System Governance**
10. ✅ **Module 10: Final Integration & Demonstration Data Suite (100% Complete Milestone)**

---

## 📋 Module Completion Summary

### What is Complete:
* **Final End-to-End Integration:** Seamless navigation between Homepage, Login, Register, Customer Dashboard, Retailer Dashboard, Inventory Console, Nearby Shops, Product Search, Price Comparison Radar, Contact Retailer Modal, and Admin Dashboard.
* **Real-Time GPS & Geolocation Engine:**
  * Native HTML5 Geolocation API (`navigator.geolocation.getCurrentPosition`) on customer dashboard.
  * Real-time distance calculation using the **Haversine Formula** ($d = 2r \arcsin(\dots)$) between customer coordinates and store locations.
  * Automatic discovery of the nearest demonstration zone (Location A Central Pune vs Location B East Pune) based on physical proximity.
  * Live GPS proximity REST API endpoint (`GET /api/location/nearest?lat=...&lng=...`).
  * Nearest-first store sorting and live distance badges (`0.35 km away (Live GPS)`).
  * Graceful fallback to predefined demo zones with user alerts when browser geolocation permission is denied or device hardware lacks GPS.
* **Demonstration Locations:** Two distinct demonstration locations (Location A: Central Pune, Location B: East Pune) with independent shop listings.
* **Demonstration Shops:** All 5 suggested shops (*Fresh Mart*, *Daily Needs Store*, *Green Basket*, *City Dairy*, *Smart General Store*) plus test edge cases (*Heritage Book* and *Anand Handloom*).
* **Sample Inventory & Multi-Store Pricing:** Diverse products spanning groceries, electronics, dairy, organics, stationery, and healthcare with varied prices, stock quantities, low-stock threshold triggers, and out-of-stock items.
* **Search & Discovery:** Case-insensitive search, partial-name matching, category filtering, and transparent rule-based AI synonym dictionary.
* **Price Comparison Logic:** Lowest-price highlighting that strictly excludes out-of-stock products from being recommended as the best available price.
* **Seeded Demo Accounts:** 1 Administrator, 3 Customers, and 5 Retailers with BCrypt hashed passwords and idempotent startup initialization.
* **Documentation & Guides:** Comprehensive README with architecture, startup commands, credentials, and step-by-step evaluation guide.

### What Remains Incomplete (Deliberate Out-of-Scope / Future Enhancements):
* **Real Payment Gateway Integration:** The current system facilitates discovery, price comparison, and retailer contact; an integrated e-commerce checkout and payment gateway is reserved for future enterprise versions.
* **Automated SMS Dispatch:** Contacting retailers uses `tel:` and WhatsApp click-to-chat links rather than paid server-side SMS gateways.
