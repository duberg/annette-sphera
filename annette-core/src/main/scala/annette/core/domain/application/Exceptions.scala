package annette.core.domain.application

import annette.core.AnnetteException

/**
 * Created by valery on 17.12.16.
 */

class ApplicationAlreadyExists extends AnnetteException("core.application.alreadyExists")

class ApplicationNotFound extends AnnetteException("core.application.notFound")
