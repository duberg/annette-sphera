package annette.core.security.authentication

import annette.core.AnnetteException

/**
 * Created by kantemirov on 15.07.16.
 */
class AuthenticationException(code: String) extends AnnetteException(code)

class UnauthorizedException(code: String) extends AuthenticationException(code)

class ForbiddenException(code: String) extends AuthenticationException(code)

class AuthenticationFailedException extends UnauthorizedException("core.authentication.exception.authenticationFailed")
class AccountDeactivatedException extends UnauthorizedException("core.authentication.exception.deactivatedAccount")

class CsrfTokenRequiredException extends UnauthorizedException("core.authentication.exception.csrfTokenRequired")

class InvalidCsrfTokenException extends UnauthorizedException("core.authentication.exception.invalidCsrfToken")

class TenantRequiredException extends UnauthorizedException("core.authentication.exception.tenantRequired")

class ApplicationRequiredException extends UnauthorizedException("core.authentication.exception.applicationRequired")

//class UserLockedException extends UnauthorizedException("core.authentication.exception.userLocked")
class UserNotFoundException extends ForbiddenException("core.authentication.exception.userNotFound")

class UserNotAssignedToTenantException extends ForbiddenException("core.authentication.exception.userNotAssignedToTenant")

//class UserLockedInTenantException extends ForbiddenException("core.authentication.exception.userLockedInTenant")
//class UserDeletedInTenantException extends ForbiddenException("core.authentication.exception.userDeletedInTenant")
class ApplicationNotFoundException extends ForbiddenException("core.authentication.exception.applicationNotFound")

//class ApplicationLockedException extends ForbiddenException("core.authentication.exception.applicationLocked")
//class ApplicationDeletedException extends ForbiddenException("core.authentication.exception.applicationDeleted")
class ApplicationNotAssignedToTenantException extends ForbiddenException("core.authentication.exception.applicationNotAssignedToTenant")
//class ApplicationLockedInTenantException extends ForbiddenException("core.authentication.exception.applicationLockedInTenant")

class TenantNotFoundException extends ForbiddenException("core.authentication.exception.tenantNotFound")
//class TenantLockedException extends ForbiddenException("core.authentication.exception.tenantLocked")
//class TenantDeletedException extends ForbiddenException("core.authentication.exception.tenantDeleted")

class LanguageNotFoundException extends ForbiddenException("core.authentication.exception.languageNotFound")

class LanguageNotAssignedToTenantException extends ForbiddenException("core.authentication.exception.languageNotAssignedToTenant")

class TenantAndApplicationNotFoundException extends ForbiddenException("core.authentication.exception.tenantAndApplicationNotFound")

class CannotCreateSessionException extends AuthenticationException("core.authentication.exception.cannotCreateSession")

class LogoutException extends AuthenticationException("core.authentication.exception.logoutFailed")
