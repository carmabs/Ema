# EMA

**EMA** is a library based on MVI architecture (Navigation and Android Architecture components), Kotlin coroutines and dependency injection based on **KODEIN**.

This way you can create by a very easy and fast way robust and maintenable apps due to **EMA** support, it handles lots of classes that helps the developer to develop new features respecting the MVI architecture.

You can se the documentation [here](https://github.com/carlosmateo89/Ema/wiki).

# USE IT

To use the library add the following dependencies to gradle:

Add the maven repository:
  
      allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
    	  }
      }

Add the **EMA** dependencies, it include ema-core library and its library dependencies.

  

    dependencies {
          implementation 'com.github.carlosmateo89.Ema:ema-android:2.3.0'
    }

If the module is pure kotlin, add the ema-core library
  

    dependencies {
	      implementation 'com.github.carlosmateo89.Ema:ema-core:2.3.0'
    }


To use **test support library** add the following ones:

    dependencies {
          implementation  'com.github.carlosmateo89.Ema:ema-testing-core:2.3.0'
          implementation  'com.github.carlosmateo89.Ema:ema-testing-android:2.3.0'
    } 

   
>***Considerations:***
>
>If you find some conflict using the library with coroutines or kotlin stuff, check you are using the same version in your project that library:
>
> * ***Coroutines : 1.3.0***
> * ***Kotlin : 1.3.60***
> 
> If you find some problems compiling library due to internal Kodein dependencies add the following code to gradle android block:
> 
	 android{
    	compileOptions {
        	sourceCompatibility JavaVersion.VERSION_1_8
        	targetCompatibility JavaVersion.VERSION_1_8
    	}
    	kotlinOptions {
        	jvmTarget = JavaVersion.VERSION_1_8.toString()
    	}
    }
