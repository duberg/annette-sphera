package annette.core.domain.application

import annette.core.AnnetteException

class ApplicationAlreadyExists extends AnnetteException("core.application.alreadyExists")
class ApplicationNotFound extends AnnetteException("core.application.notFound")
