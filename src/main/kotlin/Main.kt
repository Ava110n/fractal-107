import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.sqrt

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    Canvas(modifier = Modifier.fillMaxSize()){
        var scales = Scales(this.size.width.toInt(), this.size.height.toInt(),
        -10.0, -5.0,5.0)
        scales.xMax = 10.0
        draw_Mandelbrot(scales)

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
open class Scales(var w: Int = 0, var h: Int = 0,
    var xMin: Double = 0.0, //var xMax: Double = 0.0,
    var yMin: Double = 0.0, var yMax: Double = 0.0){

    var xMax: Double
        set(value){xMax = value}
        get() = xMax

}
class Decart(var x: Float,var y: Float):Scales(){

    fun scrToDec(s: Screen):Decart{
        println(xMax)
        var x = s.x*(xMax-xMin)/w+xMin
        var y = yMax - s.y*(yMax-yMin)/h
        return Decart(x.toFloat(),y.toFloat())
    }

}
class Screen(var x: Int,var y: Int):Scales(){
    fun decToScr(d: Decart):Screen{
        var x = (d.x-xMin)*w/(xMax-xMin)
        var y = (yMax-d.y)*h/(yMax-yMin)
        return Screen(x.toInt(),y.toInt())
    }
}
class Complex(var Re: Double, var Im: Double){
    fun pow(p: Int):Complex{
        var c = Complex(1.0, 0.0)
        for(i in 1..p){
            c *= Complex(this.Re, this.Im)
        }
        return c
    }
    operator fun times(c: Complex): Complex{
        var re = this.Re*c.Re-this.Im*c.Im
        var im = this.Re*c.Im+this.Im*c.Re
        return Complex(re,im)
    }
    operator fun plus(c: Complex): Complex{
        return Complex(this.Re+c.Re, this.Im+c.Im)
    }
    fun abs():Double{
        return sqrt(this.Re*this.Re+this.Im*this.Im)
    }
}
fun Mandelbrot(d: Decart):Int{
    var maxIter = 1000
    var R = 4.0
    var c: Complex = Complex(d.x.toDouble(), d.y.toDouble())
    var z = Complex(0.0,0.0)
    var iter = 0
    while(z.abs()<R && iter<maxIter){
        z = z.pow(2) + c
        iter++
    }
    return iter
}
fun draw_Mandelbrot(){
    for(i in 0..scales.w ){
        for(j in 0 .. scales.h){
            var s = Screen(i,j)
            var d = Decart(0f,0f)
            d = d.scrToDec(s)
            println("${s.x} ${s.y} : ${d.x} ${d.y}")
            var clr = Mandelbrot(d)
            if(clr==1000){
                drawCircle(Color.White, radius = 1f,
                    center = Offset(i.toFloat(),j.toFloat()))
            }
            else(drawCircle(Color.Black, radius = 1f,
                center = Offset(i.toFloat(),j.toFloat())))
        }
    }
}
