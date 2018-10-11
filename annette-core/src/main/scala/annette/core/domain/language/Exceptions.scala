package annette.core.domain.language

import annette.core.AnnetteException

class LanguageAlreadyExists extends AnnetteException("core.language.alreadyExists")
class LanguageNotFound extends AnnetteException("core.language.notFound")
