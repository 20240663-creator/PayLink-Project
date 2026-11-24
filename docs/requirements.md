# PayLink Requirements

## Table of Contents

1. [Project Context](#1-project-context)
2. [Stakeholder Architectural Requirements](#2-stakeholder-architectural-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [Functional Requirements (User Stories)](#4-functional-requirements-user-stories)
   - [4.1 Overview](#41-overview)
   - [4.2 Accounts and Wallet](#42-accounts-and-wallet)
   - [4.3 Biller Catalog](#43-biller-catalog)
   - [4.4 Bill Inquiry and Payment](#44-bill-inquiry-and-payment)
   - [4.5 Payment Life Cycle, Funds and Biller Integration](#45-payment-life-cycle-funds-and-biller-integration)
   - [4.6 Fees, Commissions and Fraud Controls](#46-fees-commissions-and-fraud-controls)
   - [4.7 Notifications](#47-notifications)
   - [4.8 Support, Administration and Reporting](#48-support-administration-and-reporting)

---

## 1. Project Context

PayLink is a digital wallet-based bill payment platform that enables customers to pay utility and service bills through a centralized system. It acts as an intermediary between customers and external billers, allowing users to manage their wallets, inquire about bills, and complete payments using their available balance.

The system integrates with multiple external billers, including electricity, water, mobile top-up, and internet service providers. These billers use different communication protocols and payment-processing mechanisms, requiring PayLink to handle synchronous responses, asynchronous callbacks, and batch-based settlement processes.

PayLink is responsible for managing the complete payment lifecycle, including:

- validating payment requests
- calculating service fees
- holding customer funds before submitting payments
- processing payment confirmations
- handling failures and timeouts
- releasing held funds when payments fail

It also supports agent commissions, transaction notifications, and payment reconciliation.

Since the system handles financial transactions, it must maintain accurate wallet balances, prevent duplicate charges, ensure transaction consistency, and provide reliable handling of external service failures. The architecture should also allow new billers and services to be integrated without significantly affecting existing system components.

---

## 2. Stakeholder Architectural Requirements

The PayLink architecture should address the following stakeholder requirements.

### 2.1 Customers and Agents

- Reliable bill payments with accurate wallet balance management.
- Protection against duplicate payments and incorrect deductions.
- Clear payment status updates and transaction history.
- Accurate calculation of service fees and agent commissions.

### 2.2 Administrators and Support Staff

- Ability to manage billers, services, fees, and payment limits.
- Ability to search and track transactions and investigate failed or unknown payments.
- Access to audit records and reconciliation reports.

### 2.3 External Billers

- Support for different communication protocols and data formats.
- Reliable handling of timeouts, failures, and asynchronous callbacks.
- Ability to integrate new billers or update existing integrations without modifying core payment logic.

### 2.4 Development and Operations Team

- Modular and maintainable architecture with clear component responsibilities.
- Independent testing of modules and automated test execution.
- Support for Docker, Docker Compose, and GitHub Actions CI/CD.
- Easy modification and extension of the system with minimal impact on existing components.

These requirements guide the architectural design to ensure reliability, maintainability, extensibility, and financial correctness.

---

## 3. Non-Functional Requirements

The PayLink system must satisfy the following non-functional requirements.

| ID | Quality Attribute | Requirement |
|----|-------------------|-------------|
| NFR-1 | Correctness and Consistency | The system must prevent duplicate charges and negative wallet balances, including during concurrent payment requests. All financial transactions must maintain consistent wallet and ledger records. |
| NFR-2 | Performance | The system must support at least 100 payments per second continuously for one minute when tested against mock billers. |
| NFR-3 | Resilience and Availability | Failure or unavailability of one biller must not prevent customers from making payments through other available billers. External service failures must be handled without compromising payment consistency. |
| NFR-4 | Modifiability | Adding a new biller should require changes only to the Biller Integration module and its configuration, without modifying existing business modules. |
| NFR-5 | Auditability and Traceability | Every financial movement and administrative action must have an audit record containing who performed the action, when it occurred, and why it was performed. |
| NFR-6 | Testability | The system must achieve at least 70% line coverage for domain logic, measured through the continuous integration pipeline. Critical payment scenarios must be covered by automated tests. |

---

## 4. Functional Requirements (User Stories)

### 4.1 Overview

Priorities follow the MoSCoW scheme (Must / Should / Could).

| ID | User Story | Priority | Actors |
|----|-----------|----------|--------|
| [FR-1](#fr-1-customer-registration) | Customer Registration | Must | Customer |
| [FR-2](#fr-2-upgrade-customer-verification-level) | Upgrade Customer Verification Level | Should | Customer, Support Employee |
| [FR-3](#fr-3-view-wallet-balance-and-transaction-history) | View Wallet Balance and Transaction History | Must | Customer |
| [FR-4](#fr-4-wallet-top-up) | Wallet Top-Up | Must | Customer, Agent |
| [FR-5](#fr-5-ledger-based-balance-computation) | Ledger-Based Balance Computation | Must | System |
| [FR-6](#fr-6-manage-billers-and-services) | Manage Billers and Services | Must | Admin |
| [FR-7](#fr-7-view-billers-and-required-input-fields) | View Billers and Required Input Fields | Must | Customer |
| [FR-8](#fr-8-bill-inquiry-and-fee-preview) | Bill Inquiry and Fee Preview | Must | Customer, External Biller |
| [FR-9](#fr-9-pay-bill-from-wallet) | Pay Bill from Wallet | Must | Customer, External Biller, Notification Provider |
| [FR-10](#fr-10-idempotent-payment-request) | Idempotent Payment Request | Must | Customer, Agent, System |
| [FR-11](#fr-11-enforce-payment-life-cycle-transitions) | Enforce Payment Life Cycle Transitions | Must | System |
| [FR-12](#fr-12-hold-and-release-payment-funds) | Hold and Release Payment Funds | Must | System |
| [FR-13](#fr-13-handle-biller-timeout-and-retries) | Handle Biller Timeout and Retries | Must | System, Biller |
| [FR-14](#fr-14-process-asynchronous-callbacks-from-topupnet) | Process Asynchronous Callbacks from TopUpNet | Must | TopUpNet, System |
| [FR-15](#fr-15-multi-biller-format-support) | Multi-Biller Format Support | Must | System, Billers (ElectroCo, AquaCo, TopUpNet) |
| [FR-16](#fr-16-calculate-service-fees-and-promotions) | Calculate Service Fees and Promotions | Must | System |
| [FR-17](#fr-17-agent-assisted-payment-and-commission) | Agent-Assisted Payment and Commission | Should | Agent, Customer |
| [FR-18](#fr-18-enforce-single-and-daily-payment-limits) | Enforce Single and Daily Payment Limits | Must | Customer, System |
| [FR-19](#fr-19-velocity-fraud-rule-and-temporary-block) | Velocity Fraud Rule and Temporary Block | Should | Customer, System |
| [FR-20](#fr-20-search-payment-history-by-reference) | Search Payment History by Reference | Must | Support Employee |
| [FR-21](#fr-21-block-and-unblock-customer-account) | Block and Unblock Customer Account | Should | Support Employee |
| [FR-22](#fr-22-send-sms-and-push-notifications) | Send SMS and Push Notifications | Must | Customer, Notification Service |
| [FR-23](#fr-23-retry-failed-notifications) | Retry Failed Notifications | Should | System, Notification Provider |
| [FR-24](#fr-24-dynamic-configuration-of-fees-and-limits) | Dynamic Configuration of Fees and Limits | Should | Admin |
| [FR-25](#fr-25-audit-logging-for-money-movements-and-admin-actions) | Audit Logging for Money Movements and Admin Actions | Must | System, Admin |
| [FR-26](#fr-26-daily-settlement-reconciliation-report) | Daily Settlement Reconciliation Report | Should | System, Biller, Support Employee |
| [FR-27](#fr-27-refund-request-and-support-approval) | Refund Request and Support Approval | Could | Customer, Support Employee |
| [FR-28](#fr-28-admin-daily-financial-totals) | Admin Daily Financial Totals | Could | Admin |

---

### 4.2 Accounts and Wallet

#### FR-1: Customer Registration

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer |

**User story:** As a new customer, I want to register an account with a basic verification level, so that I can get a digital wallet and start paying utility bills.

**Pre-condition:** The customer is not registered in the PayLink system.

**Post-condition:** A new customer account with Basic verification level (5,000 EGP daily limit) and an initialized wallet (0.00 EGP available and held balance) are stored in the database.

**Acceptance criteria**

1. **Successful registration**
   - **Given** a new user provides valid registration details (name, unique email/phone number, and password),
   - **When** the user submits the registration request,
   - **Then** the system creates a new account with Basic verification level, sets the daily payment limit to 5,000 EGP, initializes the wallet with 0.00 EGP available balance and 0.00 EGP held balance, and returns a success confirmation.
2. **Duplicate account**
   - **Given** an account already exists with the submitted email or phone number,
   - **When** the user submits the registration request,
   - **Then** the system rejects the request with a clear conflict error message and does not create a duplicate account.
3. **Invalid input**
   - **Given** the user submits missing or malformed registration fields,
   - **When** the user submits the registration request,
   - **Then** the system rejects the request with a validation error detailing the invalid fields.

---

#### FR-2: Upgrade Customer Verification Level

| | |
|---|---|
| **Priority** | Should |
| **Actors** | Customer, Support Employee |

**User story:** As a customer, I want to upgrade to verified status to raise my daily limit to 30,000 EGP, so that I can execute higher value utility payments.

**Pre-condition:** The customer is authenticated with an active (non-blocked) account at the Basic verification level (5,000 EGP daily limit), and a Support Employee is logged in to review upgrade requests.

**Post-condition:** If approved, the customer's account status is updated to Verified in the database and the daily payment limit is raised to 30,000 EGP. If rejected, the account remains Basic (5,000 EGP daily limit) with the rejection reason recorded.

**Acceptance criteria**

1. **Successful upgrade approval**
   - **Given** an active customer with a Basic verification level submits a verification upgrade request with valid identity details,
   - **When** a Support Employee reviews and approves the request,
   - **Then** the system updates the customer's verification level to Verified, increases the daily payment limit from 5,000 EGP to 30,000 EGP, and persists the updated status.
2. **Upgrade request rejected**
   - **Given** a pending verification upgrade request contains invalid or incomplete verification details,
   - **When** a Support Employee rejects the request with a reason,
   - **Then** the customer's verification level remains Basic with a 5,000 EGP daily payment limit, and the rejection reason is recorded.
3. **Already verified or blocked account**
   - **Given** a customer account is already Verified or currently blocked,
   - **When** the customer attempts to submit a verification upgrade request,
   - **Then** the system rejects the request with a clear error message and makes no changes to the account.

---

#### FR-3: View Wallet Balance and Transaction History

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer |

**User story:** As a customer, I want to view available balance, held balance, and transaction history, so that I can track my account activity and remaining funds.

**Pre-condition:** The customer is registered, authenticated in the PayLink system, and has an initialized EGP wallet.

**Post-condition:** The system returns the customer's exact available balance, held balance (computed from the ledger entries), and transaction history without modifying any wallet or ledger records.

**Acceptance criteria**

1. **Active wallet with transactions and holds**
   - **Given** an authenticated customer has a wallet with past ledger entries and an active hold for a pending payment,
   - **When** the customer requests to view their wallet balance and transaction history,
   - **Then** the system computes the available balance and held balance from the ledger entries using exact decimal types (no floating point) and returns them in EGP along with the chronological transaction history.
2. **Newly initialized wallet with no activity**
   - **Given** an authenticated customer has a newly created wallet with no ledger entries,
   - **When** the customer requests to view their wallet balance and transaction history,
   - **Then** the system returns 0.00 EGP for available balance, 0.00 EGP for held balance, and an empty transaction history list.
3. **Unauthorized access**
   - **Given** an unauthenticated user or a different customer attempts to view another customer's wallet details,
   - **When** the request is submitted,
   - **Then** the system rejects the request with an unauthorized/forbidden error and exposes no balance or transaction data.

---

#### FR-4: Wallet Top-Up

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, Agent |

**User story:** As a customer or agent, I want to top up wallet balance via simulated card or agent cash, so that there are sufficient funds to execute bill payments.

**Pre-condition:** The target customer has an active (non-blocked) account with an initialized EGP wallet. For cash top-ups, the agent is authenticated on the PayLink system.

**Post-condition:** A new credit ledger entry is stored for the exact decimal top-up amount, the wallet's available balance (computed from the ledger) reflects the added funds, and an audit log record (who, when, why) is written for the money movement.

**Acceptance criteria**

1. **Successful top-up via simulated card**
   - **Given** an authenticated customer with an active wallet provides a valid positive amount in EGP and valid simulated card details,
   - **When** the customer submits the top-up request,
   - **Then** the system creates a credit ledger entry using exact decimal types (no floating point), updates the computed available balance without overwriting the balance directly, records the money movement in the audit log with who, when, and why, and returns a confirmation with the updated balance.
2. **Successful top-up via agent cash**
   - **Given** an authenticated agent receives cash from a customer and submits the customer's account identifier along with a valid positive top-up amount in EGP,
   - **When** the agent submits the cash top-up request,
   - **Then** the system creates a credit ledger entry for the customer's wallet, reflects the exact increase in the customer's computed available balance, writes an audit log record, and returns a confirmation receipt.
3. **Invalid amount, failed card, or blocked account**
   - **Given** a customer or agent submits a zero or negative amount, an invalid/declined simulated card, or targets a non-existent or blocked account,
   - **When** the top-up request is submitted,
   - **Then** the system rejects the request with a clear error message, creates no ledger entry, and leaves the wallet balance unchanged.

---

#### FR-5: Ledger-Based Balance Computation

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System |

**User story:** As a system, I want to create a ledger entry for every balance change and compute balances from the ledger, so that all monetary calculations are audit-proof and consistent.

**Pre-condition:** A wallet exists in the system and a balance-affecting operation is triggered (top-up, fund hold, final payment capture, refund/release, or agent commission credit).

**Post-condition:** A new ledger entry is persisted using exact decimal types, the wallet's available and held balances are computed strictly as the sum of its ledger entries, and an audit log record (who, when, why) is written for the money movement.

**Acceptance criteria**

1. **Ledger recording and balance computation**
   - **Given** an existing wallet with recorded ledger entries,
   - **When** any balance change occurs (top-up, hold, charge, refund, or commission),
   - **Then** the system appends a new ledger entry using exact decimal types (never floating point), computes both the available balance and held balance by summing the ledger entries rather than overwriting a balance directly, and records an audit entry containing who, when, and why.
2. **Concurrent operations and zero negative balance**
   - **Given** two payment requests arrive at the same moment for the same wallet whose available balance covers only one of them,
   - **When** the system processes both balance changes concurrently,
   - **Then** one request succeeds and records its ledger entry while the other is Rejected, ensuring zero double charges and ensuring the computed wallet balance never goes negative.
3. **Invalid or aborted operation**
   - **Given** a balance-changing operation fails validation or encounters a database error mid-transaction,
   - **When** the transaction is aborted,
   - **Then** no ledger entry is committed and the wallet's computed available and held balances remain unchanged.

---

### 4.3 Biller Catalog

#### FR-6: Manage Billers and Services

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Admin |

**User story:** As an Admin, I want to add, edit, and disable billers and services, so that the platform catalog remains up to date.

**Pre-condition:** The Admin is authenticated in the PayLink system with administrative privileges.

**Post-condition:** The biller or service is created, updated, or marked as disabled in the Biller Catalog, and the admin action is recorded in the audit log.

**Acceptance criteria**

1. **Add a new biller or service**
   - **Given** an authenticated Admin provides valid details for a biller or service (name, service type, required input fields such as meter number or phone number, min and max payment amounts, and active status),
   - **When** the Admin submits the add request,
   - **Then** the system persists the new biller/service in the Biller Catalog, writes the admin action to the audit log, and returns a success confirmation.
2. **Edit an existing biller or service**
   - **Given** an authenticated Admin selects an existing biller or service and submits valid updates to its details (such as required input fields or min/max amounts),
   - **When** the Admin submits the edit request,
   - **Then** the system updates the stored catalog record, logs the admin action in the audit log, and applies the updated configuration to future customer requests.
3. **Disable a biller or service**
   - **Given** an authenticated Admin selects an active biller or service,
   - **When** the Admin disables it,
   - **Then** the system marks the biller or service as disabled, prevents customers from initiating new inquiries or payments for it, writes the admin action to the audit log, and preserves all historical transaction records.
4. **Unauthorized access or invalid data**
   - **Given** a non-admin user attempts to manage the catalog, or an Admin submits missing/invalid fields (e.g., minimum amount exceeding maximum amount or a duplicate biller identifier),
   - **When** the request is submitted,
   - **Then** the system rejects the request with a clear authorization or validation error and leaves the Biller Catalog unchanged.

---

#### FR-7: View Billers and Required Input Fields

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer |

**User story:** As a Customer, I want to see the list of active billers and the input fields each requires, so that I can submit accurate bill payment details.

**Pre-condition:** The customer is authenticated in the PayLink system, and the Biller Catalog is initialized with biller and service definitions.

**Post-condition:** The system returns the active billers, their services, required input fields, and configured minimum/maximum payment amounts without modifying any catalog data.

**Acceptance criteria**

1. **View active billers and required input fields**
   - **Given** the Biller Catalog contains active billers and services (such as ElectroCo for electricity bills requiring a meter number, AquaCo for water bills, and TopUpNet for mobile top-up requiring a phone number),
   - **When** the customer requests the list of available billers,
   - **Then** the system returns all active billers and services along with the exact required input fields and the minimum and maximum payment amounts for each.
2. **Disabled billers excluded**
   - **Given** an Admin has marked a biller or service as disabled in the Biller Catalog,
   - **When** the customer requests the list of available billers,
   - **Then** the system excludes the disabled biller or service from the response so the customer cannot initiate new inquiries or payments for it.
3. **Empty active catalog**
   - **Given** there are no active billers currently enabled in the Biller Catalog,
   - **When** the customer requests the list of available billers,
   - **Then** the system returns an empty list with a valid success response and no system error.

---

### 4.4 Bill Inquiry and Payment

#### FR-8: Bill Inquiry and Fee Preview

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, External Biller |

**User story:** As a customer, I want to run a bill inquiry with an active biller and see the amount due, the service fee, and the total cost before paying, so that I can review the full cost before authorizing a payment.

**Pre-condition:** The customer is authenticated in the PayLink system, and the selected biller and service are active in the Biller Catalog and support live bill inquiry.

**Post-condition:** The system displays the bill amount due, the calculated service fee (based on configured fee rules or an active promotion), and the total payable amount in EGP using exact decimal types, without creating any ledger entry or holding funds from the wallet.

**Acceptance criteria**

1. **Successful bill inquiry and fee preview**
   - **Given** an authenticated customer selects an active live biller (e.g., ElectroCo) and provides valid required input fields (e.g., meter number),
   - **When** the customer runs a bill inquiry,
   - **Then** PayLink queries the external biller for the amount due (e.g., 350.00 EGP), calculates the applicable service fee according to configured rules (e.g., fixed 2.50 EGP for electricity and water), computes the total (352.50 EGP) using exact decimal types (never floating point), and displays the amount, fee, and total before paying without modifying the wallet balance.
2. **Active promotion fee preview**
   - **Given** a promotion period (within its configured start and end dates) is active for the selected biller,
   - **When** the customer runs a bill inquiry for that biller,
   - **Then** the system calculates the service fee as 0.00 EGP, sets the total equal to the bill amount due, and displays the full breakdown before payment.
3. **Invalid input, biller timeout, or unsupported live inquiry**
   - **Given** the customer enters an invalid bill reference, the external biller fails to respond within the 10-second live biller timeout (or returns an error), or the selected biller does not support live inquiry (e.g., NetCo batch),
   - **When** the customer submits the bill inquiry,
   - **Then** the system returns a clear error message explaining the issue and makes no changes to the customer's wallet.

---

#### FR-9: Pay Bill from Wallet

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, External Biller, Notification Provider |

**User story:** As a customer, I want to pay a utility bill directly from my digital wallet, so that my bill is settled with the biller without visiting a physical office while my funds are safely managed.

**Pre-condition:** The customer is authenticated with an active (non-blocked) account, has sufficient available wallet balance to cover the bill amount plus the service fee, the selected biller/service is active, and the request includes a unique idempotency key.

**Post-condition:**

- On biller confirmation, the payment reaches **Completed**, the held funds are converted into a final charge via ledger entries (never overwriting the balance directly), PayLink's fee income is credited, an audit log entry (who, when, why) is written, and the customer is sent an SMS and push notification.
- If rejected before calling the biller, the payment ends in **Rejected** with no balance change.
- If refused by the biller, it transitions to **Failed** and then **Refunded** once the hold is released.
- If it times out after 10 seconds, it becomes **Unknown** and funds remain held.

**Acceptance criteria**

1. **Successful bill payment**
   - **Given** an authenticated customer has an active account with sufficient available balance and passes all fraud and limit checks (single payment between 5 EGP and 10,000 EGP, within the daily limit, and within the velocity rule),
   - **When** the customer submits a payment request with valid bill details and an idempotency key,
   - **Then** the system moves the total (bill amount + service fee) using exact decimal types from available balance to held balance (Initiated → FundsHeld), sends the payment with a unique reference to the external biller (SentToBiller), and upon biller confirmation transitions the payment to Completed, turns the hold into a final charge via ledger entries, credits PayLink's fee income, records the money movement in the audit log, and triggers an SMS and push notification receipt.
2. **Insufficient funds or failed fraud/limit checks**
   - **Given** a customer submits a bill payment request when their available wallet balance is insufficient, their account is blocked, or the payment violates the single-payment (5–10,000 EGP), daily (5,000 EGP basic / 30,000 EGP verified), or velocity limit,
   - **When** the system validates the payment request,
   - **Then** the payment transitions from Initiated to Rejected with a clear reason, no funds are held or deducted, and the wallet balance never goes negative.
3. **Biller refuses payment**
   - **Given** funds have been held and the payment has been sent to the external biller (SentToBiller),
   - **When** the external biller refuses the payment with an error code,
   - **Then** the payment transitions to Failed, the system releases the held funds back to the customer's available balance via a ledger entry (Refunded), writes an audit log entry, and notifies the customer via SMS and push message with a clear failure reason.
4. **Biller timeout**
   - **Given** funds have been held and the payment has been sent to a live external biller (SentToBiller),
   - **When** the biller does not respond within the 10-second timeout,
   - **Then** the payment transitions to Unknown, the funds remain held (never refunded automatically), and the customer is informed that the payment is still being processed.

---

#### FR-10: Idempotent Payment Request

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, Agent, System |

**User story:** As a customer or agent submitting a payment request over an unreliable network, I want repeated requests carrying the same idempotency key to return the original payment result without executing a second charge, so that I am never charged twice for the same bill.

**Pre-condition:** The customer or agent is authenticated and submits a payment request containing a unique idempotency key.

**Post-condition:** Exactly one payment record and one corresponding wallet charge/hold exist for that idempotency key, and any subsequent request with the same key returns the stored original result without creating additional ledger entries or calling the external biller again.

**Acceptance criteria**

1. **Duplicate request after the original payment was processed (Hard Case #1)**
   - **Given** a payment request with a specific idempotency key has already been processed by PayLink,
   - **When** the client (e.g., mobile app after a slow network response) sends the same payment request again with the exact same idempotency key,
   - **Then** the system detects the existing key, executes no second charge, creates no new ledger entries, makes no duplicate call to the external biller, and returns the original payment result.
2. **Concurrent duplicate requests with the same key**
   - **Given** an initial payment request with a specific idempotency key is currently in progress,
   - **When** a second identical request with the same idempotency key arrives concurrently,
   - **Then** the system prevents concurrent duplicate execution, guarantees zero double charges and zero negative balances, and returns the original payment's outcome/status once resolved.
3. **Missing idempotency key or key reused with different parameters**
   - **Given** a payment request is submitted without an idempotency key, or reuses an existing idempotency key with different payment details (such as a different biller, amount, or bill reference),
   - **When** the system validates the request,
   - **Then** the system rejects the request with a clear validation/conflict error and makes no changes to the wallet or existing payment record.

---

### 4.5 Payment Life Cycle, Funds and Biller Integration

#### FR-11: Enforce Payment Life Cycle Transitions

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System |

**User story:** As a system, I want to enforce strict payment state machine transitions, so that payment statuses remain consistent and valid.

**Pre-condition:** A payment exists and is in exactly one life cycle state (Initiated, FundsHeld, Rejected, SentToBiller, Completed, Failed, Unknown, or Refunded), and an event occurs that could change its state (fraud check result, funds check, request sent to the biller, biller response, timeout, callback, retry result, reconciliation result, or hold release).

**Post-condition:** The payment is still in exactly one state. Its state changes only when the event matches one of the nine permitted transitions; any other requested transition is refused and leaves the state unchanged. Completed, Rejected, and Refunded remain final, and a payment in Unknown is never moved directly to Refunded.

**Permitted transitions**

| # | From | To | Trigger |
|---|------|----|---------|
| 1 | Initiated | FundsHeld | Fraud checks pass and the wallet has enough funds |
| 2 | Initiated | Rejected | A fraud check fails or funds are insufficient |
| 3 | FundsHeld | SentToBiller | The request is sent to the biller |
| 4 | SentToBiller | Completed | The biller confirms |
| 5 | SentToBiller | Failed | The biller refuses |
| 6 | SentToBiller | Unknown | No answer within the timeout |
| 7 | Unknown | Completed | The biller later confirms (callback, retry, or settlement file) |
| 8 | Unknown | Failed | The biller later refuses, or reconciliation finds no payment |
| 9 | Failed | Refunded | The hold is released to the wallet |

**Acceptance criteria**

1. **Permitted transitions**
   - **Given** a payment is in a source state of the life cycle,
   - **When** the matching event occurs,
   - **Then** the system moves the payment to the target state listed in the table above.
2. **Transition not in the life cycle**
   - **Given** a payment in any state,
   - **When** an event requests a transition that is not one of the nine permitted transitions (for example Initiated → SentToBiller, FundsHeld → Completed, SentToBiller → Refunded, Completed → Failed, or Failed → Completed),
   - **Then** the system refuses the transition, keeps the payment in its current state, and creates no ledger entry.
3. **Final states**
   - **Given** a payment is in a final state (Completed, Rejected, or Refunded),
   - **When** a later event tries to change it (for example a late biller refusal for a Completed payment),
   - **Then** the system keeps the payment in its final state and creates no ledger entry.
4. **Unknown is never refunded automatically**
   - **Given** a payment is in Unknown,
   - **When** time passes or all retries are used without a definitive biller answer,
   - **Then** the payment stays in Unknown with its funds held, and it leaves Unknown only by moving to Completed or Failed on the events listed above; it is never refunded directly from Unknown.

---

#### FR-12: Hold and Release Payment Funds

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System |

**User story:** As a system, I want to hold funds before calling the biller and release them if the payment fails, so that customer funds are protected against unconfirmed debits.

**Pre-condition:** A payment is in the Initiated state with a calculated total (bill amount + service fee), the customer's wallet exists, and the payment is processed according to the standard payment processing steps.

**Post-condition:** Funds are held through ledger entries (a balance is never overwritten): the available balance decreases and the held balance increases by the payment total before the biller is called. On biller confirmation the hold becomes a final charge. On failure the held amount is released back to the available balance. While the payment is Unknown, the funds stay held.

**Acceptance criteria**

1. **Hold before calling the biller**
   - **Given** a payment in Initiated has passed the fraud checks and the customer's available balance covers the total (for example 1,000.00 EGP available and a total of 352.50 EGP),
   - **When** the system processes the payment,
   - **Then** it records ledger entries that move 352.50 EGP from the available balance to the held balance (647.50 EGP available, 352.50 EGP held), moves the payment to FundsHeld, and only after the hold is recorded sends the request to the biller (SentToBiller).
2. **Insufficient funds**
   - **Given** the customer's available balance is lower than the total (for example 300.00 EGP available and a total of 352.50 EGP),
   - **When** the system processes the payment,
   - **Then** the payment moves from Initiated to Rejected with an insufficient-funds reason, no funds are held, the biller is not called, and the balances are unchanged.
3. **Hold released when the biller refuses (Hard Case 3)**
   - **Given** a payment is in SentToBiller with 352.50 EGP held,
   - **When** the biller refuses the payment (for example AquaCo returns an error code) and the payment becomes Failed,
   - **Then** the system releases the held 352.50 EGP back to the available balance through a ledger entry, reduces the held balance by 352.50 EGP, and moves the payment from Failed to Refunded.
4. **Hold becomes a final charge on success**
   - **Given** a payment is in SentToBiller with its total held,
   - **When** the biller confirms and the payment becomes Completed,
   - **Then** the held amount becomes a final charge through ledger entries and no part of it is released back to the available balance.
5. **Funds stay held while Unknown**
   - **Given** a payment is in SentToBiller with its total held,
   - **When** the biller does not answer within the timeout and the payment becomes Unknown,
   - **Then** the held amount stays held, nothing is released to the available balance, and the funds are only charged or released after the payment later becomes Completed or Failed.
6. **Hold cannot be recorded**
   - **Given** the system cannot record the hold (for example a database error occurs during the ledger update),
   - **When** the transaction is aborted,
   - **Then** no ledger entry is committed, the available and held balances remain unchanged, and the request is not sent to the biller.

---

#### FR-13: Handle Biller Timeout and Retries

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System, Biller |

**User story:** As a system, I want to mark timed-out payments as Unknown after 10 seconds and re-query after 1, 5, and 15 minutes, so that transient network delays are resolved cleanly.

**Pre-condition:** A payment is in SentToBiller for a live biller (such as ElectroCo, AquaCo, or TopUpNet) and its total is held in the customer's wallet.

**Post-condition:** A payment that gets no biller answer within 10 seconds is in Unknown with its funds still held. Unknown payments are re-queried at most 3 times, at the 1-minute, 5-minute, and 15-minute retry offsets. A confirmation moves the payment to Completed, a refusal moves it to Failed, and if no definitive answer arrives the payment waits for reconciliation. No refund is made automatically.

**Acceptance criteria**

1. **Timeout becomes Unknown (Hard Case 2)**
   - **Given** a payment is in SentToBiller for a live biller with 352.50 EGP held,
   - **When** the biller does not answer within 10 seconds (for example ElectroCo takes 12 seconds),
   - **Then** the payment moves from SentToBiller to Unknown, the 352.50 EGP stays held, no refund is made, and the customer is told that the payment is being processed.
2. **Answer within the timeout**
   - **Given** a payment is in SentToBiller for a live biller (for example AquaCo, which answers in 2 to 4 seconds),
   - **When** the biller answers within 10 seconds,
   - **Then** the payment follows the biller's answer (Completed or Failed) and no retry is scheduled.
3. **Re-query confirms the payment**
   - **Given** a payment is in Unknown,
   - **When** a re-query at one of the retry offsets (1, 5, or 15 minutes) returns a biller confirmation,
   - **Then** the payment moves from Unknown to Completed, the hold becomes a final charge, and no further re-query is made.
4. **Re-query shows the biller refused**
   - **Given** a payment is in Unknown,
   - **When** a re-query at one of the retry offsets returns a biller refusal,
   - **Then** the payment moves from Unknown to Failed, the held amount is released to the available balance, the payment moves from Failed to Refunded, and no further re-query is made.
5. **Retries exhausted**
   - **Given** a payment is in Unknown and has been re-queried 3 times (at the 1, 5, and 15-minute offsets) without a definitive biller answer,
   - **When** the third re-query ends,
   - **Then** the system makes no further re-query, the payment stays in Unknown with its funds held until reconciliation resolves it, and it is not refunded automatically.

---

#### FR-14: Process Asynchronous Callbacks from TopUpNet

| | |
|---|---|
| **Priority** | Must |
| **Actors** | TopUpNet, System |

**User story:** As a system, I want to accept async callbacks from TopUpNet and ignore duplicate callbacks, so that asynchronous bill payment updates are processed reliably.

**Pre-condition:** A mobile top-up payment has been sent to TopUpNet with a unique payment reference and its total is held in the customer's wallet. TopUpNet has replied "accepted" and will later send a callback containing "success" or "failed".

**Post-condition:** Each TopUpNet payment completes or fails at most once. A duplicate callback causes no second state change, no additional ledger entry, no second fee posting, and no second commission posting, and every duplicate callback is logged.

**Acceptance criteria**

1. **Accepted reply, result pending**
   - **Given** a payment is sent to TopUpNet with its total held,
   - **When** TopUpNet replies "accepted",
   - **Then** the payment is not marked Completed or Failed, the funds stay held, and the system waits for the callback.
2. **Callback "success"**
   - **Given** a TopUpNet payment is waiting for its result (in SentToBiller, or in Unknown after a timeout),
   - **When** a callback with "success" arrives for its payment reference,
   - **Then** the payment moves to Completed, the hold becomes a final charge, and PayLink's fee income and any agent commission are posted once.
3. **Callback "failed"**
   - **Given** a TopUpNet payment is waiting for its result (in SentToBiller, or in Unknown after a timeout),
   - **When** a callback with "failed" arrives for its payment reference,
   - **Then** the payment moves to Failed, the held amount is released to the available balance, and the payment moves from Failed to Refunded.
4. **Duplicate callback (Hard Case 6)**
   - **Given** a payment has already been completed by a "success" callback,
   - **When** TopUpNet sends the same "success" callback for that payment again,
   - **Then** the payment is not completed a second time, no ledger entry, fee income, or commission is posted again, the duplicate is ignored, and the duplicate callback is logged. The same applies to a repeated "failed" callback: the hold is released only once.
5. **Unmatched or invalid callback**
   - **Given** a callback arrives that references no existing payment, or contains a result other than "success" or "failed",
   - **When** the system processes the callback,
   - **Then** no payment state and no ledger entry is changed.

---

#### FR-15: Multi-Biller Format Support

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System, Billers (ElectroCo, AquaCo, TopUpNet) |

**User story:** As a system, I want to support at least 3 billers with different protocols/formats (REST JSON, SOAP XML, Async REST), so that PayLink seamlessly integrates across diverse partner APIs.

**Pre-condition:** ElectroCo, AquaCo, and TopUpNet are configured as active billers in the Biller Catalog, each with its own integration adapter, and a payment for one of them is in FundsHeld and ready to be sent.

**Post-condition:** Each payment is sent in the format of its biller:

| Biller | Service | Protocol / Format |
|--------|---------|-------------------|
| ElectroCo | Electricity | REST/JSON |
| AquaCo | Water | SOAP-style XML over HTTP |
| TopUpNet | Mobile top-up | REST/JSON with asynchronous callbacks |

Each biller's response, including its own field names and error codes, is translated into the same payment outcomes (confirmed, refused, no answer), so the rest of the payment flow does not depend on the biller's protocol.

**Acceptance criteria**

1. **ElectroCo: REST/JSON**
   - **Given** an active ElectroCo electricity service and a payment in FundsHeld,
   - **When** the system sends the payment,
   - **Then** the request is sent as REST/JSON with the payment's unique reference, and ElectroCo's answer is translated into a confirmation or a refusal.
2. **AquaCo: SOAP-style XML**
   - **Given** an active AquaCo water service and a payment in FundsHeld,
   - **When** the system sends the payment,
   - **Then** the request is sent as SOAP-style XML over HTTP using AquaCo's field names, AquaCo's answer (2 to 4 seconds is within the 10-second timeout) is translated into a confirmation or a refusal, and AquaCo's error codes are translated into a refusal reason.
3. **TopUpNet: REST/JSON with callback**
   - **Given** an active TopUpNet mobile top-up service and a payment in FundsHeld,
   - **When** the system sends the payment,
   - **Then** the request is sent as REST/JSON, the "accepted" reply is treated as a pending result, and the final "success" or "failed" callback is handled as described in FR-14.
4. **Biller refuses with an error code (Hard Case 3)**
   - **Given** a payment has been sent to AquaCo,
   - **When** AquaCo refuses it with an error code (for example because the payment is above the bill amount),
   - **Then** the payment moves to Failed, the hold is released, and the customer is given a clear failure reason.
5. **One biller unavailable**
   - **Given** ElectroCo is unavailable or not answering,
   - **When** a customer pays an AquaCo or TopUpNet bill,
   - **Then** that payment is processed normally, and only ElectroCo payments are affected (they move to Unknown according to FR-13).
6. **Adding a new biller (Hard Case 8)**
   - **Given** a new biller is to be supported,
   - **When** the team adds it,
   - **Then** only a new integration adapter and configuration are added, and the existing modules (such as Payment Processing, Accounts and Wallet, and Fees and Commissions) are not modified.

---

### 4.6 Fees, Commissions and Fraud Controls

#### FR-16: Calculate Service Fees and Promotions

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System |

**User story:** As a system, I want to calculate fixed, percentage, tiered, and zero-fee promotional rates using exact decimals, so that billing charges match precise business pricing rules.

**Pre-condition:** The selected service has a fee rule stored in configuration (not in code), the bill amount is known, and any promotion has configurable start and end dates for one chosen biller.

**Post-condition:** The service fee is calculated according to the configured fee rules using exact decimal types (no floating point), the total payable equals the bill amount plus the fee, and the calculation itself changes no wallet balance and creates no ledger entry.

**Fee rules summary**

| Service | Rule | Example |
|---------|------|---------|
| Electricity | Fixed 2.50 EGP | 350.00 EGP bill → 2.50 EGP fee (total 352.50 EGP) |
| Water | Fixed 2.50 EGP | 120.00 EGP bill → 2.50 EGP fee (total 122.50 EGP) |
| Mobile top-up | 1.5% of amount, minimum 1.00 EGP | 100.00 EGP → 1.50 EGP; 50.00 EGP → 1.00 EGP |
| Internet | Tiered | 200.00 EGP → 2.00 EGP; 200.01 EGP and 500.00 EGP → 4.00 EGP |
| Promotion | 0.00 EGP fee for one chosen biller within a configured date range | Total equals bill amount |

**Acceptance criteria**

1. **Fixed fee: electricity and water**
   - **Given** an electricity bill of 350.00 EGP or a water bill of 120.00 EGP,
   - **When** the fee is calculated,
   - **Then** the fee is 2.50 EGP in both cases (totals 352.50 EGP and 122.50 EGP).
2. **Percentage fee: mobile top-up**
   - **Given** a mobile top-up of 100.00 EGP,
   - **When** the fee is calculated,
   - **Then** the fee is 1.5% of the amount, which is 1.50 EGP (total 101.50 EGP).
3. **Mobile top-up minimum fee**
   - **Given** a mobile top-up of 50.00 EGP (1.5% would be 0.75 EGP),
   - **When** the fee is calculated,
   - **Then** the fee is the 1.00 EGP minimum (total 51.00 EGP).
4. **Tiered fee: internet**
   - **Given** internet bills of 200.00 EGP, 200.01 EGP, and 500.00 EGP,
   - **When** the fee is calculated for each,
   - **Then** the fees are 2.00 EGP, 4.00 EGP, and 4.00 EGP respectively.
5. **Promotion period**
   - **Given** a promotion is configured for one chosen biller (for example ElectroCo) with a start date and an end date,
   - **When** a fee is calculated for that biller on a date inside the promotion period,
   - **Then** the fee is 0.00 EGP and the total equals the bill amount; and when the fee is calculated on a date before the start date or after the end date, or for a different biller, the normal fee for the service applies.
6. **Values come from configuration**
   - **Given** the fixed electricity fee in configuration is changed from 2.50 EGP to 3.00 EGP,
   - **When** a fee is calculated for an electricity bill,
   - **Then** the fee is 3.00 EGP without any code change.

---

#### FR-17: Agent-Assisted Payment and Commission

| | |
|---|---|
| **Priority** | Should |
| **Actors** | Agent, Customer |

**User story:** As an Agent, I want to pay bills on behalf of a customer and earn 40% of the service fee as commission, so that agent channels are incentivized to process payments.

**Pre-condition:** The agent is authenticated with an agent account and wallet, the customer has an active (non-blocked) account with an initialized wallet, the selected biller and service are active, and the request includes an idempotency key.

**Post-condition:** The payment is funded from the customer's wallet and follows the standard payment life cycle. When it reaches Completed, the agent's wallet is credited once with 40% of the service fee through a ledger entry (with an audit log record). In every other case no commission is credited.

**Acceptance criteria**

1. **Successful agent-assisted payment**
   - **Given** an authenticated agent and a customer with 1,000.00 EGP available, and an electricity bill of 350.00 EGP with a 2.50 EGP fee,
   - **When** the agent submits the payment on behalf of the customer,
   - **Then** 352.50 EGP is held from the customer's wallet (not the agent's wallet), and when the biller confirms and the payment becomes Completed, the agent's wallet is credited 1.00 EGP (40% of 2.50 EGP) through a ledger entry and the commission is recorded against the agent.
2. **No commission before completion**
   - **Given** an agent-assisted payment is Rejected, Failed, Refunded, or still in FundsHeld, SentToBiller, or Unknown,
   - **When** the payment has not reached Completed,
   - **Then** no commission is credited to the agent; and if an Unknown payment later becomes Completed, the commission is credited at that moment.
3. **Commission credited once**
   - **Given** an agent-assisted payment has become Completed and its commission has been credited,
   - **When** the completion is signaled again (for example a duplicate TopUpNet callback or a repeated request with the same idempotency key),
   - **Then** no second commission is credited.
4. **Promotion with zero fee**
   - **Given** an agent pays a bill for a biller that has an active promotion (fee 0.00 EGP),
   - **When** the payment becomes Completed,
   - **Then** the commission is 0.00 EGP and the agent's wallet balance is unchanged.
5. **Customer has insufficient funds**
   - **Given** the customer's available balance is lower than the payment total,
   - **When** the agent submits the payment,
   - **Then** the payment moves from Initiated to Rejected with an insufficient-funds reason, the agent's wallet is not used, and no commission is credited.
6. **Agent cash top-up is a separate operation**
   - **Given** a customer gives cash to an agent to top up the wallet,
   - **When** the agent submits the cash top-up,
   - **Then** the system handles it as the wallet top-up operation of FR-4: it creates no bill payment, no service fee, and no commission.

---

#### FR-18: Enforce Single and Daily Payment Limits

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, System |

**User story:** As a system, I want to reject payments outside the 5–10,000 EGP single limit or above the daily limit with a clear reason, so that platform risk exposure remains strictly controlled.

**Pre-condition:** A payment is in the Initiated state, the customer has a Basic (5,000 EGP daily limit) or Verified (30,000 EGP daily limit) verification level, and the global limits (5 EGP minimum and 10,000 EGP maximum per payment) and each service's own minimum and maximum (inside the global range) are configured.

**Post-condition:** A payment that respects every limit continues to the remaining checks. A payment that breaks a limit moves from Initiated to Rejected with a reason that names the limit it broke; no funds are held, no ledger entry is created, and the biller is not called.

**Limits summary**

| Limit | Value |
|-------|-------|
| Single payment minimum (global) | 5 EGP |
| Single payment maximum (global) | 10,000 EGP |
| Service minimum / maximum | Configured per service, inside the global range |
| Daily limit, Basic customer | 5,000 EGP |
| Daily limit, Verified customer | 30,000 EGP |

**Acceptance criteria**

1. **Payment within all limits**
   - **Given** a Basic customer whose payments already counted today total 1,000.00 EGP and a payment of 350.00 EGP that is inside the global range and the service's own range,
   - **When** the system checks the limits,
   - **Then** the limit check passes and the payment continues to the remaining checks.
2. **Above the single-payment maximum**
   - **Given** a customer submits a payment of 12,000.00 EGP,
   - **When** the system checks the limits,
   - **Then** the payment moves from Initiated to Rejected with a reason stating that the 10,000 EGP single-payment maximum is exceeded, and no funds are held.
3. **Below the single-payment minimum**
   - **Given** a customer submits a payment of 1.00 EGP,
   - **When** the system checks the limits,
   - **Then** the payment moves from Initiated to Rejected with a reason stating that the 5 EGP single-payment minimum is not met, and no funds are held; a payment of 5.00 EGP is not rejected for the minimum.
4. **Service minimum or maximum**
   - **Given** a service is configured with its own maximum of 3,000.00 EGP (inside the global range),
   - **When** a customer submits a payment of 4,000.00 EGP for that service,
   - **Then** the payment moves from Initiated to Rejected with a reason stating the service's maximum of 3,000.00 EGP, even though 4,000.00 EGP is below the 10,000 EGP global maximum.
5. **Basic customer daily limit**
   - **Given** a Basic customer whose payments already counted today total 4,900.00 EGP,
   - **When** the customer submits a payment of 500.00 EGP,
   - **Then** the payment moves from Initiated to Rejected with a reason stating that the 5,000 EGP daily limit is exceeded, and no funds are held.
6. **Verified customer daily limit**
   - **Given** a Verified customer whose payments already counted today total 4,900.00 EGP,
   - **When** the customer submits a payment of 500.00 EGP,
   - **Then** the daily limit check passes because the limit is 30,000 EGP.
   - **Given** a Verified customer whose payments already counted today total 29,800.00 EGP,
   - **When** the customer submits a payment of 500.00 EGP,
   - **Then** the payment moves from Initiated to Rejected with a reason stating that the 30,000 EGP daily limit is exceeded.

---

#### FR-19: Velocity Fraud Rule and Temporary Block

| | |
|---|---|
| **Priority** | Should |
| **Actors** | Customer, System |

**User story:** As a system, I want to block an account for 10 minutes if more than 5 payments occur in 60 seconds, so that automated rapid-fire fraud attempts are halted.

**Pre-condition:** The customer's account is active and not currently blocked, and the customer submits payments in quick succession.

**Post-condition:** When more than 5 payments occur within 60 seconds from one account, the velocity check fails, the payment that triggered it is Rejected, and the account is blocked for 10 minutes. While the block lasts, every payment request from that account is Rejected with a clear reason. When the 10 minutes have passed, the velocity block no longer rejects payments.

**Acceptance criteria**

1. **Five payments are allowed**
   - **Given** an account has submitted 5 payments within 60 seconds and each was accepted,
   - **When** the system applies the velocity rule,
   - **Then** none of the 5 payments is rejected by the velocity rule and the account is not blocked.
2. **Sixth payment triggers the block**
   - **Given** an account has had 5 payments accepted within the last 60 seconds,
   - **When** the account submits a 6th payment within the same 60 seconds,
   - **Then** the velocity check fails, the 6th payment moves from Initiated to Rejected with a reason stating that more than 5 payments were made in 60 seconds, no funds are held, and the account is blocked for 10 minutes.
3. **Payments while blocked**
   - **Given** an account was blocked by the velocity rule 4 minutes ago,
   - **When** the account submits any payment,
   - **Then** the payment moves from Initiated to Rejected with a reason stating that the account is temporarily blocked, no funds are held, and the biller is not called.
4. **Block ends after 10 minutes**
   - **Given** an account was blocked by the velocity rule and 10 minutes have passed,
   - **When** the account submits a payment that passes the other checks,
   - **Then** the velocity block does not reject it and the payment continues to the remaining checks.
5. **Slower payments are not blocked**
   - **Given** an account submits 6 payments spaced 20 seconds apart (never more than 5 within any 60 seconds),
   - **When** the system applies the velocity rule,
   - **Then** no payment is rejected by the velocity rule and the account is not blocked.

---

### 4.7 Notifications

#### FR-22: Send SMS and Push Notifications

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Customer, Notification Service |

**User story:** As a Customer, I want to receive an SMS and push notification when a payment completes or fails, so that I am immediately informed of transaction outcomes.

**Pre-condition:** The customer has an account with a registered phone number and a device configured to receive push notifications. A payment transaction has been initiated.

**Post-condition:** The customer receives an SMS and a push notification containing the payment outcome.

**Acceptance criteria**

1. **Outcome notification**
   - **Given** a customer has initiated a payment,
   - **When** the payment is completed or fails,
   - **Then** the Notification Service sends the customer both an SMS and a push notification informing them of the transaction outcome.

---

#### FR-23: Retry Failed Notifications

| | |
|---|---|
| **Priority** | Should |
| **Actors** | System, Notification Provider |

**User story:** As a system, I want to retry failed SMS/email notifications 3 times then mark them undelivered without blocking payment, so that messaging failures do not interrupt core transaction flows.

**Pre-condition:** A payment transaction has triggered a notification, and the notification delivery attempt has failed.

**Post-condition:** The system retries sending the failed notification up to 3 times. If all attempts fail, the notification is marked as undelivered, while the payment process remains unaffected.

**Acceptance criteria**

1. **Retries exhausted**
   - **Given** a notification delivery attempt has failed,
   - **When** the system retries sending the notification up to 3 times and all attempts fail,
   - **Then** the system marks the notification as undelivered without interrupting or affecting the payment process.

---

### 4.8 Support, Administration and Reporting

#### FR-20: Search Payment History by Reference

| | |
|---|---|
| **Priority** | Must |
| **Actors** | Support Employee |

**User story:** As a Support Employee, I want to search a payment by reference and view its full state history, so that customer inquiries and issues can be resolved rapidly.

**Pre-condition:** The support employee is logged into the system and has permission to access payment information.

**Post-condition:** The system displays the payment's full state history if a matching payment reference is found. Otherwise, the system displays an appropriate message indicating that no payment was found.

**Acceptance criteria**

1. **Search by reference**
   - **Given** a support employee is logged in and has a valid payment reference,
   - **When** they search for that reference,
   - **Then** the system displays the matching payment and its complete state history in chronological order.

---

#### FR-21: Block and Unblock Customer Account

| | |
|---|---|
| **Priority** | Should |
| **Actors** | Support Employee |

**User story:** As a Support Employee, I want to block and unblock a customer account, so that suspicious accounts can be isolated and verified safely.

**Pre-condition:** The support employee is logged into the system, has permission to manage customer accounts, and the target customer account exists.

**Post-condition:** The customer's account status is updated to blocked or active, depending on the action performed.

**Acceptance criteria**

1. **Block an account**
   - **Given** a support employee is viewing an active customer account,
   - **When** they choose to block the account,
   - **Then** the system changes its status to blocked.

---

#### FR-24: Dynamic Configuration of Fees and Limits

| | |
|---|---|
| **Priority** | Should |
| **Actors** | Admin |

**User story:** As an Admin, I want to edit fee and limit values without redeploying the application, so that operational business rules can be tuned instantly in real time.

**Pre-condition:** The admin is logged into the system and has permission to modify fee and limit configurations.

**Post-condition:** The updated fee and limit values are saved and applied by the system without requiring an application redeployment.

**Acceptance criteria**

1. **Update configuration**
   - **Given** an admin is logged into the system and has permission to manage configurations,
   - **When** they modify and save fee or limit values,
   - **Then** the system saves and applies the updated values immediately without requiring an application redeployment.

---

#### FR-25: Audit Logging for Money Movements and Admin Actions

| | |
|---|---|
| **Priority** | Must |
| **Actors** | System, Admin |

**User story:** As a system, I want to write every money movement and admin action to an audit log with who, when, and why, so that complete regulatory compliance and traceability are guaranteed.

**Pre-condition:** A money movement or admin action is performed within the system.

**Post-condition:** An audit log entry is created containing the responsible user, the date and time, and the reason for the action.

**Acceptance criteria**

1. **Record every action**
   - **Given** a money movement or admin action is performed,
   - **When** the action is processed by the system,
   - **Then** the system automatically records it in the audit log, including who performed it, when it occurred, and why it was performed.

---

#### FR-26: Daily Settlement Reconciliation Report

| | |
|---|---|
| **Priority** | Should |
| **Actors** | System, Biller, Support Employee |

**User story:** As a system, I want to compare PayLink records with nightly settlement files and flag discrepancies, so that financial imbalances are surfaced and resolved daily.

**Pre-condition:** The system has access to PayLink transaction records and has received the daily settlement file from the Biller.

**Post-condition:** A daily reconciliation report is generated, containing the comparison results and any identified discrepancies.

**Acceptance criteria**

1. **Daily reconciliation**
   - **Given** the system has access to PayLink transaction records and has received the settlement file,
   - **When** the daily reconciliation process runs,
   - **Then** the system compares the records, identifies and flags any discrepancies, and generates a daily reconciliation report.

---

#### FR-27: Refund Request and Support Approval

| | |
|---|---|
| **Priority** | Could |
| **Actors** | Customer, Support Employee |

**User story:** As a customer, I want to request a refund of a completed payment for support to approve, so that valid erroneous transactions can be safely reversed.

**Pre-condition:** The customer is logged into the system and has at least one completed payment eligible for a refund request.

**Post-condition:** A refund request is submitted and recorded as pending until a support employee reviews and approves or rejects it.

**Acceptance criteria**

1. **Submit a refund request**
   - **Given** a customer has a completed payment,
   - **When** they submit a refund request for that payment,
   - **Then** the system records the request as pending and makes it available to a support employee for review and approval or rejection.

---

#### FR-28: Admin Daily Financial Totals

| | |
|---|---|
| **Priority** | Could |
| **Actors** | Admin |

**User story:** As an Admin, I want to view daily totals for payments, fees earned, commissions paid, and failures per biller, so that business health and platform revenue performance are clear.

**Pre-condition:** The admin is logged into the system and has permission to access financial reports.

**Post-condition:** The system displays a daily financial summary showing payments, fees earned, commissions paid, and failures for each biller.

**Acceptance criteria**

1. **View daily totals**
   - **Given** an admin is logged into the system and has access to financial reports,
   - **When** they request the daily financial totals,
   - **Then** the system displays the total payments, fees earned, commissions paid, and failed transactions for each biller for the selected day.
