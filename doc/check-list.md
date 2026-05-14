# Quick Testing Checklist

Simple check-list to keep focus on main testing areas according to testing pyramid.

> 💡 **Need more details?** See [testing-guide.md](testing-guide.md) for comprehensive, actionable checklists.


## Frontend
- FE component has unit/integration tests validating specific component's logic
- FE component has unit/integration tests validating consumer logic based on received contract
- FE component has unit/integration tests validating produced contract
## Backend:
- BE service has integration tests validating produced contract
- BE service has integration tests validating consumer logic based on received contract
- BE service has unit/integration tests validating specific service's logic

## Integration:
- UI tests are present validating FE + BE integration (real services deployment)

## Documentation:
- Actual versions of contracts are uploaded to defined common repository

## Testing Coverage:
- Produced contracts are validated with full testing coverage (positive/negative/specific use-cases)
- Consumed contracts are validated with positive use-cases and negative use-cases (ex. no mandatory field received)
- UI tests are validating happy-path/main use-cases
