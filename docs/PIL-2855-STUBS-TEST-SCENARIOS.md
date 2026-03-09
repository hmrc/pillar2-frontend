# PIL-2855: Stub setup for testing all scenarios

Instructions for an agent working in the **pillar2-stubs** repository to configure stubs so every PIL-2855 scenario can be tested against the pillar2-frontend multi-period implementation.

## Frontend behaviour (pillar2-frontend)

- **Feature flag:** `amendMultipleAccountingPeriods` in `conf/application.conf` (e.g. `amendMultipleAccountingPeriods = true` for multi-period).
- **Flag OFF:** Manage group details summary calls existing subscription/cache flow and shows single-period CYA view.
- **Flag ON:** Manage group details summary calls **Display Subscription V2**, then shows the multi-period view (location row + accounting period cards with Change links). Clicking a period’s “Change” caches that period and redirects to the **Data Entry** page: `/manage-account/account-details/change-accounting-period`.

## API contract the frontend uses

### Display Subscription V2 (flag ON)

- **Method/URL:** `GET .../report-pillar2-top-up-taxes/subscription/read-subscription/v2/:userId/:plrReference`  
  (or the stub equivalent under the same path prefix used by the frontend `SubscriptionConnector`.)

- **Response shape:** JSON with at least:
  - `accountingPeriod`: array of objects, each with:
    - `startDate`, `endDate`, `dueDate` (e.g. `"yyyy-MM-dd"`)
    - `canAmendStartDate`, `canAmendEndDate` (boolean)
  - Location: from UPE details, e.g. domestic-only flag so frontend can show “Only in the UK” vs “In the UK and outside the UK”.
  - Other fields required by `SubscriptionDataV2` (e.g. `formBundleNumber`, `upeDetails`, `upeCorrespAddressDetails`, `primaryContactDetails`, etc.).

- **Amendable periods:** Frontend only shows periods where **both** `canAmendStartDate` and `canAmendEndDate` are `true`, and sorts by `endDate` descending (most recent first).

### Subscription cache (user-cache)

- Frontend reads/writes subscription cache (e.g. `user-cache` or equivalent in stubs) so that after V2 is called the data (including `accountingPeriods` and location) is available for the multi-period view and for the Data Entry page after “Change” is clicked.
- If stubs are used as the full backend for local runs, ensure GET/POST for the subscription user-cache paths used by the frontend are stubbed so cache read/write works.

## Scenarios to support in stubs

| # | Scenario | Flag | Actor | Stub requirement |
|---|----------|------|--------|-------------------|
| 1 | Multiple accounting periods – no micro periods | On | Group (org) | One plrReference returns V2 response with 2+ amendable periods (e.g. “Current” and “Previous”), no micro-period. |
| 2 | Multiple accounting periods – with micro period | On | Group | One plrReference returns V2 with at least one micro-period (shorter than 12 months) and other amendable periods. |
| 3 | Multiple accounting periods – no micro periods | On | Agent | Same as 1 but request is in agent context (same V2 response; frontend shows Group name + Group ID in header). |
| 4 | Multiple accounting periods – micro period | On | Agent | Same as 2 but in agent context. |
| 5 | Single accounting period | Off | Group / Agent | Frontend uses existing flow; stub only needs to support the **old** Display Subscription (V1) and cache so the single-period summary page works. |
| 6 | Change location (Where are the group entities located?) | On | Group / Agent | V2 response includes correct location (e.g. domestic-only true/false); frontend shows “Only in the UK” or “In the UK and outside the UK” and Change link to MneOrDomestic. |
| 7 | Change location | Off | Group / Agent | Same location data via **old** Display Subscription (V1) and cache. |

## Concrete stub implementation hints

1. **V2 route**  
   Add a route in pillar2-stubs that matches the frontend’s V2 URL (see `SubscriptionConnector.displaySubscriptionV2` in pillar2-frontend).  
   Example path pattern:  
   `GET .../subscription/read-subscription/v2/:id/:plrReference`  
   Return JSON that conforms to the Display Subscription V2 schema (e.g. `SubscriptionDataV2` / `SubscriptionSuccessV2` in frontend).

2. **Test PILLAR2 IDs (plrReference)**  
   - **Multi-period (no micro):** e.g. `XEPLR8888888888` – V2 response with 2+ periods, all with `canAmendStartDate` and `canAmendEndDate` true, normal 12-month lengths.  
   - **Multi-period (with micro):** e.g. `XEPLR9999999999` – V2 response including at least one period with length &lt; 12 months and both amend flags true.  
   - **Single period (flag OFF):** Use existing IDs that work with the current (V1) Display Subscription and cache.

3. **Location (Scenarios 6 & 7)**  
   In both V1 and V2 responses, set the “domestic only” (or equivalent) field so that:
   - “Domestic only” = true → frontend shows “Only in the UK”.
   - “Domestic only” = false → frontend shows “In the UK and outside the UK”.

4. **User-cache**  
   If stubs implement the subscription user-cache:
   - GET must return stored subscription data (or a default) so the summary page can load.
   - POST must accept and store the body so that after “Change” (which updates cache with the selected period) the Data Entry page at `/manage-account/account-details/change-accounting-period` receives the correct cached period.

5. **Empty state**  
   To test “no accounting periods available to amend”, provide a V2 response where `accountingPeriod` is either empty or no element has both `canAmendStartDate` and `canAmendEndDate` true.

## Quick checklist for the stubs agent

- [ ] Add V2 route: `GET .../subscription/read-subscription/v2/:id/:plrReference` and return valid V2 JSON.
- [ ] Define at least two test plrReferences: one for 2+ normal amendable periods, one for at least one micro-period.
- [ ] Ensure location (domestic only) is set correctly in V1 and V2 for “Only in the UK” / “In the UK and outside the UK”.
- [ ] (If applicable) Stub subscription user-cache GET/POST so cache read/write matches frontend.
- [ ] Document the test IDs and expected behaviour (e.g. in README or a test-scenarios doc) so QA can run Scenarios 1–7 against pillar2-frontend with flag on/off.
