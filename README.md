# Normative Control for Data Access in Healthcare Research

Implementation code for PhD thesis on normative control for data access in healthcare research.

## Repository Structure

### Duty_Enforcement/
A Policy Enforcement Point (PEP) system for managing data sharing duties in healthcare research contexts. This is a Java/Kotlin web application built with Maven that handles duty messaging, lifecycle management, and policy enforcement for data access scenarios.

**Note:** For access to the full Duty Enforcement repository, please contact **Thomas van Binsbergen** (l.t.vanbinsbergen@uva.nl).

### ODRL-policies/
Sample policies written in [ODRL (Open Digital Rights Language)](https://www.w3.org/TR/odrl-model/), a policy expression language for representing normative statements. These policies demonstrate various use cases:

- **delegation1-3.json** - Policies demonstrating ownership transfer and delegated permission granting between parties
- **duty1-2.json** - Policies showing duty assignments and conditional permissions based on obligations
- **Partygranurality.json** - Example demonstrating party granularity limitations in ODRL
- **transformational.json** - Policy showing transformational aspects (e.g., loans with time-based conditions)
- **conflict.json** - Example of policy conflicts with geographic constraints and exceptions

### eFLINT_data_sharing_agreement_policies/
Data sharing agreement policies written in [eFLINT](https://gitlab.com/eflint), a domain-specific language for formalizing norms based on transition systems and Hohfeld's framework of legal fundamental concepts. Examples include:

- **grant_access.eflint** - Access granting based on consortium membership
- **transfer_and_read__1_.eflint** - Ownership transfer agreements between parties
- **access_purpose1.eflint** - Role and purpose-based access control
- **condition_for_access.eflint** - Conditional access policies
- **definition_DSA.eflint** - Data sharing agreement definitions
- **give_permission.eflint** - Permission granting policies
- **amdex_prototype/** - AMdEX prototype scenarios for sharing insights and policy requests

## License

See [LICENSE](LICENSE) for details.
