# EMA

**EMA** is a library based on MVVM architecture (Navigation and Android Architecture components), Kotlin coroutines and dependency injection based on **KODEIN**.

This way you can create by a very easy and fast way robust and maintenable apps due to **EMA** support, it handles lots of classes that helps the developer to develop new features respecting the MVVM architecture.

You can se the documentation [here](https://github.com/carlosmateo89/Ema/wiki).

# USE IT

To use the library add the following dependencies to gradle:

Add the maven repository:
  >
      allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
    	  }
      }

Add the **EMA** dependencies
  >

    dependencies {
	      implementation 'com.github.carlosmateo89.Ema:ema-core:1.0.0'
          implementation 'com.github.carlosmateo89.Ema:ema-android:1.0.0'
    }

To use **test support library** add the following ones:

  >

    dependencies {
          implementation  'com.github.carlosmateo89.Ema:ema-testing-core:1.0.0'
          implementation  'com.github.carlosmateo89.Ema:ema-testing-android:1.0.0'
    }
