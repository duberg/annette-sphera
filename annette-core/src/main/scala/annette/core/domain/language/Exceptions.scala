package annette.core.domain.language

import annette.core.AnnetteException

/**
 * Created by valery on 17.12.16.
 */

class LanguageAlreadyExists extends AnnetteException("core.language.alreadyExists")

class LanguageNotFound extends AnnetteException("core.language.notFound")
