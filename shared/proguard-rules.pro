-keep class ai.lerna.multiplatform.Lerna {*;}
-keep class ai.lerna.multiplatform.service.actionML.dto.Result {*;}
-keep class ai.lerna.multiplatform.service.actionML.converter.RecommendationConverter {*;}
-dontwarn java.lang.invoke.StringConcatFactory

# Keep names on serializable classes
-keepnames class ai.lerna.multiplatform.service.actionML.dto.* {*;}
-keepnames class ai.lerna.multiplatform.service.dto.* {*;}

-printmapping ./build/mapping.txt
