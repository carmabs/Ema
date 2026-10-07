package com.carmabs.ema.android.extension

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.BundleSerializer
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.value.EmaUriType
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

data class NameInitializer(val name: String) : EmaInitializer

object NameInitializerSerializer : KSerializer<NameInitializer> {
    override val descriptor = PrimitiveSerialDescriptor("NameInitializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: NameInitializer) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder) = NameInitializer(decoder.decodeString())
}

data class SerializableInitializer(val id: Int) :
    EmaInitializer,
    Serializable

data class ParcelableInitializer(val id: Int) :
    EmaInitializer,
    Parcelable {
    override fun describeContents() = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeInt(id)

    companion object CREATOR : Parcelable.Creator<ParcelableInitializer> {
        override fun createFromParcel(source: Parcel) = ParcelableInitializer(source.readInt())
        override fun newArray(size: Int) = arrayOfNulls<ParcelableInitializer>(size)
    }
}

@RunWith(RobolectricTestRunner::class)
class EmaInitializerExtensionsTest {

    private val kSerialization = BundleSerializerStrategy.kSerialization(NameInitializerSerializer)

    @Test
    fun `kotlin serialization strategy saves and restores the initializer`() {
        val bundle = NameInitializer("Ana").toBundle(kSerialization)!!
        assertEquals("\"Ana\"", bundle.getString(EmaInitializer.KEY))
        assertEquals(NameInitializer("Ana"), bundle.getInitializer<NameInitializer>(kSerialization))
        assertEquals(NameInitializer("Eva"), kSerialization.fromStringValue("\"Eva\""))
        assertNull(kSerialization.restore(Bundle()))
    }

    @Test
    fun `serializable strategy saves and restores the initializer`() {
        val strategy = BundleSerializerStrategy.serializable<SerializableInitializer>()
        val bundle = Bundle().apply { setInitializer(SerializableInitializer(1), strategy) }
        assertEquals(SerializableInitializer(1), bundle.getInitializer<SerializableInitializer>(strategy))
        assertEquals("SerializableInitializer(id=1)", strategy.toStringValue(SerializableInitializer(1)))
        assertEquals(EmaInitializer.EMPTY, strategy.fromStringValue("x"))
    }

    @Test
    fun `parcelable strategy saves and restores the initializer`() {
        val strategy = BundleSerializerStrategy.parcelable<ParcelableInitializer>()
        val serializer = BundleSerializer(Bundle(), strategy)
        serializer.save(ParcelableInitializer(7))
        assertEquals(ParcelableInitializer(7), serializer.restore())
        assertEquals("ParcelableInitializer(id=7)", strategy.toStringValue(ParcelableInitializer(7)))
        assertEquals(EmaInitializer.EMPTY, strategy.fromStringValue("x"))
    }

    @Test
    fun `empty strategy keeps nothing`() {
        val strategy = BundleSerializerStrategy.EMPTY
        val bundle = Bundle()
        strategy.save(NameInitializer("a"), bundle)
        assertTrue(bundle.isEmpty)
        assertNull(strategy.restore(bundle))
        assertEquals("", strategy.toStringValue(NameInitializer("a")))
        assertEquals(EmaInitializer.EMPTY, strategy.fromStringValue("a"))
    }

    @Test
    fun `initializer bundles for navigation`() {
        assertNull((null as EmaInitializer?).toBundle(kSerialization))
        assertNull((null as EmaInitializerBundle?).toBundle())
        val bundle = EmaInitializerBundle(NameInitializer("Ana"), kSerialization).toBundle()!!
        assertEquals(NameInitializer("Ana"), bundle.getInitializer<NameInitializer>(kSerialization))
    }

    @Test
    fun `intents and activities carry the initializer`() {
        val intent = Intent().setInitializer(NameInitializer("Ana"), kSerialization)
        assertEquals(NameInitializer("Ana"), intent.getInitializer<NameInitializer>(kSerialization))

        val activity = Robolectric.buildActivity(Activity::class.java, intent).setup().get()
        assertEquals(NameInitializer("Ana"), activity.getInitializer<NameInitializer>(kSerialization, null))
        activity.setInitializer(NameInitializer("Eva"), kSerialization)
        assertEquals(NameInitializer("Eva"), activity.getInitializer<NameInitializer>(kSerialization, null))
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class EmaResourceExtensionsTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `texts are resolved with the context`() {
        val ok = context.getString(android.R.string.ok)
        assertEquals(ok, EmaText.id(android.R.string.ok).string(context))
        assertEquals("Hi Ana", EmaText.text("Hi %s", "Ana").string(context))
        assertEquals("plain 50%", EmaText.text("plain 50%").string(context))
        assertEquals("$ok!", EmaText.composition(EmaText.id(android.R.string.ok), EmaText.text("!")).string(context))
        assertEquals(EmaText.id(android.R.string.ok), android.R.string.ok.toEmaText())
        assertEquals(EmaText.plural(1, 2, "x"), 1.toEmaText(2, "x"))
        assertEquals(ok, android.R.string.ok.getFormattedString(context))
        assertEquals("Hi Ana", "Hi %s".getFormattedString("Ana"))
    }

    @Test
    fun `resources are read with the context`() {
        assertEquals(Color.BLACK, android.R.color.black.getColor(context))
        assertTrue(android.R.dimen.app_icon_size.getDimension(context) > 0f)
        assertTrue(android.R.drawable.ic_delete.getDrawable(context).intrinsicWidth > 0)
        assertEquals(0, 1.getAttributeDimenValue(context))
        assertEquals("#00FF00", Color.GREEN.toHex())
    }

    @Test
    fun `drawables are converted to bitmaps`() = runTest {
        val drawable = android.R.drawable.ic_delete
        val intrinsic = drawable.getDrawable(context)
        assertEquals(20, drawable.getBitmap(context, width = 20, height = 10).width)
        assertEquals(10, drawable.getBitmap(context, width = 20, height = 10).height)
        assertEquals(20, drawable.getBitmap(context, width = 20).width)
        assertEquals(10, drawable.getBitmap(context, height = 10).height)
        assertEquals(intrinsic.intrinsicWidth, drawable.getBitmap(context).width)
        assertEquals(30, drawable.getBitmapFromResource(context, 30, 30, Color.RED).width)
        assertEquals(intrinsic.intrinsicWidth, drawable.getBitmapFromResource(context).width)
        val crop = drawable.getBitmapCropFromResource(context)
        assertEquals(crop.width, crop.height)
        assertTrue(drawable.getByteArray(context).isNotEmpty())
    }

    @Test
    fun `missing resources fail`() = runTest {
        assertFailsWith<Exception> { 0.getBitmapFromResource(context) }
    }

    @Test
    fun `resource uris point to the resource`() {
        val uri = android.R.drawable.ic_delete.toUriRes(context, EmaUriType.Drawable)
        assertTrue(uri.value.startsWith("android.resource://"))
        assertTrue(uri.value.endsWith("ic_delete"))
        assertEquals(android.R.drawable.ic_delete, uri.getResourceId(context, "drawable"))
        assertEquals(android.R.drawable.ic_delete, uri.requireResourceDrawable(context))
    }

    @Test
    fun `uri actions run only for their type`() {
        val drawable = android.R.drawable.ic_delete.toUriRes(context, EmaUriType.Drawable)
        val color = android.R.color.black.toUriRes(context, EmaUriType.Color)
        val string = android.R.string.ok.toUriRes(context, EmaUriType.String)
        val calls = mutableListOf<String>()

        drawable.onDrawable(context) { calls.add("drawable") }
            .onBitmap(context, 10, 10) { calls.add("bitmap ${width}x$height") }
            .onColor(context) { calls.add("color") }
            .onString(context) { calls.add("string") }
        color.onColor(context) { calls.add("color $this") }.onDrawable(context) { calls.add("drawable") }
        string.onString(context) { calls.add("string $this") }
            .onPlural(context, 1) { calls.add("plural") }
            .onAsset(context) { calls.add("asset") }
            .onRaw(context) { calls.add("raw") }

        assertEquals(
            listOf(
                "drawable",
                "bitmap 10x10",
                "color ${Color.BLACK}",
                "string ${context.getString(android.R.string.ok)}"
            ),
            calls
        )
        assertEquals(10, drawable.requireBitmap(context, 10, 10).width)
    }
}

@RunWith(RobolectricTestRunner::class)
class EmaContextExtensionsTest {

    @Test
    fun `activities are found through context wrappers`() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val wrapper = ContextWrapper(ContextWrapper(activity))
        assertSame(activity, wrapper.findActivity())
        assertSame(activity, wrapper.findComponentActivity())
    }

    @Test
    fun `contexts without activity fail`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertFailsWith<IllegalStateException> { context.findActivity() }
        assertFailsWith<IllegalStateException> { context.findComponentActivity() }
        val plainActivity = Robolectric.buildActivity(Activity::class.java).setup().get()
        assertFailsWith<IllegalStateException> { plainActivity.findComponentActivity() }
    }

    @Test
    fun `bundles and intents read parcelable and serializable values`() {
        val bundle = Bundle().apply {
            putParcelable("p", ParcelableInitializer(1))
            putSerializable("s", SerializableInitializer(2))
        }
        assertEquals(ParcelableInitializer(1), bundle.getParcelableCompat<ParcelableInitializer>("p"))
        assertEquals(SerializableInitializer(2), bundle.getSerializableCompat<SerializableInitializer>("s"))
        assertNull(bundle.getParcelableCompat<ParcelableInitializer>("missing"))

        val intent = Intent().putExtra("p", ParcelableInitializer(3)).putExtra("s", SerializableInitializer(4))
        assertEquals(ParcelableInitializer(3), intent.getParcelableExtraCompat<ParcelableInitializer>("p"))
        assertEquals(SerializableInitializer(4), intent.getSerializableExtraCompat<SerializableInitializer>("s"))
    }

    @Test
    fun `display conversions`() {
        val density = android.content.res.Resources.getSystem().displayMetrics.density
        assertEquals((10 * density).toInt(), 10.dp)
        assertEquals(10 * density, 10f.dp)
        assertTrue(10.sp > 0)
        assertTrue(10f.sp > 0f)
        val configuration = Configuration().apply {
            screenWidthDp = 100
            screenHeightDp = 200
            densityDpi = 320
        }
        assertEquals(200f, configuration.screenWidthPx)
        assertEquals(400f, configuration.screenHeightPx)
    }

    @Test
    fun `screen metrics and status bar`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(getScreenMetrics(context).widthPixels > 0)
        assertTrue(getScreenMetrics(context, includeInsets = true).heightPixels > 0)
        assertTrue(context.getStatusBarHeight() >= 0)
    }

    @Test
    fun `sizes fit proportionally`() {
        assertEquals(50 to 100, fitInTargetMaxSizeProportionally(100, 200, 100, 100))
        assertEquals(100 to 50, fitInTargetMaxSizeProportionally(200, 100, 100, 100))
        val drawable = android.R.drawable.ic_delete.getDrawable(ApplicationProvider.getApplicationContext())
        assertEquals(
            drawable.intrinsicWidth to drawable.intrinsicHeight,
            drawable.fitInTargetMaxSizeProportionally(null, null)
        )
    }
}
