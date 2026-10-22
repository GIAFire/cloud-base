# Member

The Member context owns customer identities and the credentials used to authenticate them in user-facing applications.

## Language

**Member**:
A tenant-scoped person who can authenticate to a user-facing application.
_Avoid_: Admin user, operator

**Email Credential**:
A normalized, tenant-unique email address paired with a password for Member authentication.
_Avoid_: Username, mailbox account

**Email Verification Code**:
A short-lived, single-use proof that a person controls an email address for one specific verification purpose.
_Avoid_: Login token, reusable PIN
