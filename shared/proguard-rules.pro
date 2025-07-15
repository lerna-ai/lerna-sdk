-keep class ai.lerna.multiplatform.Lerna {*;}
-keep class ai.lerna.multiplatform.ModelData {*;}
-keep class ai.lerna.multiplatform.MergeInputData {*;}
-keep class ai.lerna.multiplatform.utils.MathExtensionsKt {*;}
-keep class ai.lerna.multiplatform.Sensors {*;}
-keep class ai.lerna.multiplatform.config.UserID {*;}
-keep class ai.lerna.multiplatform.service.EncryptionService {*;}
-keep class ai.lerna.multiplatform.service.Storage {*;}
-keep class ai.lerna.multiplatform.service.StorageImpl {*;}
-keep class ai.lerna.multiplatform.service.MLInferenceNewModel {*;}
-keep class ai.lerna.multiplatform.service.MLInference {*;}
-keep class ai.lerna.multiplatform.service.FileUtil {*;}
-keep class ai.lerna.multiplatform.service.advancedML.** {*;}
-keep class ai.lerna.multiplatform.service.converter.DLArrayConverter {*;}
-keep class ai.lerna.multiplatform.service.actionML.dto.Result {*;}
-keep class ai.lerna.multiplatform.service.actionML.converter.RecommendationConverter {*;}
-keep class ai.lerna.multiplatform.utils.CalculationUtil {*;}
-dontwarn java.lang.invoke.StringConcatFactory

# Keep names on serializable classes
-keepnames class ai.lerna.multiplatform.service.actionML.dto.* {*;}
-keepnames class ai.lerna.multiplatform.service.dto.* {*;}

-printmapping ./build/mapping.txt
