<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>
   
    <@kt.addAllKotlinDependencies />
    <instantiate from="src/app_package/ViewModel.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${functionalityName}ViewModel.kt" />

    <#if addActivity>
    <instantiate from="src/app_package/EMAViewActivity.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${functionalityName}Activity.kt" />
    </#if>
    <instantiate from="src/app_package/EMAViewFragment.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${functionalityName}Fragment.kt" />

    <instantiate from="src/app_package/ViewState.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${functionalityName}State.kt" />

    <#if createNavigator>
    <instantiate from="src/app_package/Navigator.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${functionalityName}Navigator.kt" />
    </#if>
      
</recipe>