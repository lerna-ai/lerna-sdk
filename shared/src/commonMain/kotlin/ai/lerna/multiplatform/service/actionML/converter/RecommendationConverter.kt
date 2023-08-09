package ai.lerna.multiplatform.service.actionML.converter

import ai.lerna.multiplatform.service.actionML.dto.Result

interface RecommendationConverter {
	fun convert(item: Result): Any
}