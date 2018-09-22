/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.language

import annette.core.exception.AnnetteException

/**
 * Created by valery on 17.12.16.
 */

class LanguageAlreadyExists extends AnnetteException("core.language.alreadyExists")

class LanguageNotFound extends AnnetteException("core.language.notFound")
