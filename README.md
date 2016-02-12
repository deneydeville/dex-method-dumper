# dex-method-dumper 

Print dex tables module from APK. Extraced dex method dump module from [ClassyShark](https://github.com/google/android-classyshark).

Really easy

```kotlin
   @JvmStatic 
   fun main(args: Array<String>) {
      println(MethodDumper().dumpMethods(File("/Users/bfarber/Desktop/Scenarios/4 APKs/com.android.chrome-52311111.apk")))
   }
```
