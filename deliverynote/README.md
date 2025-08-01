# AARSOMA GRAPHICS DELIVERY NOTE SYSTEM

## Project Overview

This JavaFX application manages delivery notes for AARSOMA GRAPHICS with the following features:
- Welcome splash screen
- Dashboard with statistics (total, yearly, monthly, weekly, daily)
- Customer management
- Delivery note creation
- Delivery history with search and print options


## Build Configuration

The application will be built using Gradle with the JavaFX plugin and JPackage for creating Windows executables.

## UI Wireframes

1. **Splash Screen**:
    - "AARSOMA GRAPHICS DELIVERY NOTE SYSTEM" logo and title
    - NEXT button to proceed to dashboard

2. **Dashboard**:
    - Statistics panels showing counts (total, yearly, monthly, weekly, daily)
    - Menu bar with Customer, Delivery Challan, and Delivery History options
    - System date and time display

3. **Delivery Note Form**:
    - Auto-generated serial number
    - Customer selection dropdown with add new option
    - Current system date and time (auto-filled)
    - Table for items with columns: Sr. No, Items, Ordered Qty, Delivered Qty, Balance Qty
    - Print preview button

## Technology Stack

- **Frontend**: JavaFX 17
- **Database**: H2 Database (embedded mode)
- **Build Tool**: Gradle
- **Packaging**: jpackage for creating native Windows executable
- **Architecture**: MVC (Model-View-Controller)
- **Dependencies**:
    - JavaFX 17
    - H2 Database
    - JasperReports (for reporting and printing)
    - ControlsFX (for enhanced UI components)
    - Lombok (to reduce boilerplate code)

## Project Structure

```
com.aarsoma.deliverynote/
├── Main.java                            # Application entry point
├── config/
│   ├── DBConfig.java                    # Database configuration
│   └── AppConfig.java                   # Application settings
├── model/
│   ├── Customer.java                    # Customer entity
│   ├── DeliveryNote.java                # Delivery note entity
│   ├── DeliveryItem.java                # Items in a delivery note
│   ├── FinancialYear.java               # Financial year utility
│   └── Statistics.java                  # Dashboard statistics model
├── repository/
│   ├── CustomerRepository.java          # Customer data operations
│   ├── DeliveryNoteRepository.java      # Delivery note data operations
│   └── DeliveryItemRepository.java      # Delivery item data operations
├── service/
│   ├── CustomerService.java             # Customer business logic
│   ├── DeliveryNoteService.java         # Delivery note business logic
│   ├── StatisticsService.java           # Dashboard statistics logic
│   ├── PrintService.java                # Printing functionality
│   └── DateTimeService.java             # Date/Time utility service
├── controller/
│   ├── SplashScreenController.java      # Splash screen controller
│   ├── DashboardController.java         # Dashboard controller
│   ├── CustomerController.java          # Customer management controller
│   ├── DeliveryNoteController.java      # Delivery note creation controller
│   ├── DeliveryHistoryController.java   # History and search controller
│   └── PrintPreviewController.java      # Print preview controller
├── view/
│   ├── splash.fxml                      # Splash screen view
│   ├── dashboard.fxml                   # Dashboard view
│   ├── customer.fxml                    # Customer management view
│   ├── deliverynote.fxml                # Delivery note creation view
│   ├── deliveryhistory.fxml             # History and search view
│   ├── printpreview.fxml                # Print preview view
│   └── css/
│       └── style.css                    # Application styles
└── util/
    ├── AlertUtil.java                   # Alert dialogs utility
    ├── ValidationUtil.java              # Input validation utility
    └── PrintUtil.java                   # Printing utility
```

## Database Schema

```sql
-- Customers table
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Delivery Notes table
CREATE TABLE delivery_notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    note_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    issue_date TIMESTAMP NOT NULL,
    financial_year VARCHAR(9) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Delivery Items table
CREATE TABLE delivery_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    delivery_note_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    ordered_qty INT NOT NULL,
    delivered_qty INT NOT NULL,
    balance_qty INT NOT NULL,
    FOREIGN KEY (delivery_note_id) REFERENCES delivery_notes(id)
);
```

## Key Features Implemented

1. **Dashboard Statistics**
    - Real-time display of total, financial year, monthly, weekly, and daily delivery note counts
    - Auto-updating system date and time display
    - Menu bar with easy access to all functionality

2. **Delivery Note Creation**
    - Auto-generated incremental serial numbers
    - System date and time auto-filled
    - Customer selection with ability to add new customer
    - Table-based item entry with calculation of balance quantities
    - Print preview with A5 formatted delivery notes

3. **Search and History**
    - Search by customer name
    - Search by specific date
    - Search by date range
    - Print capability for past delivery notes

4. **Printing**
    - A5 format with tearable receipt section
    - Dashed line for tearing
    - "This is a system generated Delivery Note & does not require a physical signature" text above tear line
    - Signature column below tear line

## Technical Implementation

1. **Database Design**
    - Tables for customers, delivery notes, and delivery items
    - Foreign key relationships for data integrity
    - Optimized queries for statistics and search operations

2. **Business Logic**
    - Financial year calculation (April 1 to March 31)
    - Proper transaction management
    - Data validation

3. **Packaging**
    - Gradle build system with JavaFX plugin
    - JPackage for creating Windows executable
    - Self-contained application with embedded database

## Building and Running

To build the application, you can use:

```bash
./gradlew clean build
```

To create a Windows executable:

```bash
./gradlew createWindowsExe
```

The executable will be created in the `dist` directory, ready for distribution to end users.
