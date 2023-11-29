import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var scales by remember { mutableStateOf(Scales(0f,0f,-2.0,2.0,-2.0,2.0)) }
    var scroll by remember { mutableStateOf(false)}

    Canvas(modifier = Modifier.fillMaxSize().onPointerEvent(PointerEventType.Scroll){
        scales.w = this.size.width.toFloat()
        scales.h = this.size.height.toFloat()
        var pos = it.changes.first().position
        scales.rescale(pos, 0.9)
        scroll = true
        //println(scroll)
        //draw_Mandelbrot(this., scales)
    }){
        scales.w = this.size.width
        scales.h = this.size.height
        draw_Mandelbrot(this, scales)
        println(scroll)
        scroll = false
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

class Scales(var w: Float = 0f, var h: Float = 0f,
    var xMin: Double = -5.0, var xMax: Double = 5.0,
    var yMin: Double = -5.0, var yMax: Double = 5.0){

    fun rescale(pos: Offset, k: Double){
        var sxMin = pos.x-this.w*k/2
        var sxMax = pos.x+this.w*k/2
        if(pos.x - this.w*k/2 < 0){
            sxMax += this.w*k/2-pos.x
            sxMin = 0.0
        }
        if(pos.x + this.w*k/2 > this.w){
            sxMin += this.w-pos.x-this.w*k/2
            sxMax = this.w*k
        }

        var syMin = pos.y+this.h*k/2
        var syMax = pos.y-this.h*k/2
        if(pos.y - this.h*k/2 < 0){
            syMin += this.h*k/2-pos.y
            syMax = 0.0
        }
        if(pos.y + this.h*k/2 > this.h){
            syMax += this.h-pos.y-this.h*k/2
            syMin = this.h*k
        }
        var oldScale = this
        this.xMin = Screen(sxMin.toFloat(), 0f).scrToDec(oldScale).x.toDouble()
        this.xMax = Screen(sxMax.toFloat(), 0f).scrToDec(oldScale).x.toDouble()
        this.yMin = Screen(0f, syMin.toFloat()).scrToDec(oldScale).y.toDouble()
        this.yMax = Screen(0f, syMax.toFloat()).scrToDec(oldScale).y.toDouble()

    }

}
class Decart(var x: Float,var y: Float){
    fun scrToDec(scales: Scales):Decart{
        var x = this.x*(scales.xMax-scales.xMin)/scales.w+scales.xMin
        var y = scales.yMax - this.y*(scales.yMax-scales.yMin)/scales.h
        return Decart(x.toFloat(),y.toFloat())
    }
    fun decToScr(d: Decart, scales: Scales):Screen{
        var x = (d.x-scales.xMin)*scales.w/(scales.xMax-scales.xMin)
        var y = (scales.yMax-d.y)*scales.h/(scales.yMax-scales.yMin)
        return Screen(x.toFloat(),y.toFloat())
    }
}
class Screen(var x: Float, var y: Float){
    fun decToScr(d: Decart, scales: Scales):Screen{
        var x = (d.x-scales.xMin)*scales.w/(scales.xMax-scales.xMin)
        var y = (scales.yMax-d.y)*scales.h/(scales.yMax-scales.yMin)
        return Screen(x.toFloat(),y.toFloat())
    }
    fun scrToDec(scales: Scales):Decart{
        var x = this.x*(scales.xMax-scales.xMin)/scales.w+scales.xMin
        var y = scales.yMax - this.y*(scales.yMax-scales.yMin)/scales.h
        return Decart(x.toFloat(),y.toFloat())
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
fun draw_Mandelbrot(scope: DrawScope, scales: Scales){
    //var scales = Scales(scope.size.width.toInt(), scope.size.height.toInt(),
        //-2.0, 2.0,-2.0, 2.0)
    //scales.xMax = 10.0

    for(i in 0..scales.w.toInt() ){
        for(j in 0 .. scales.h.toInt()){
            var s = Screen(i.toFloat(),j.toFloat())
            var d = s.scrToDec(scales)
            //println("${s.x} ${s.y} : ${d.x} ${d.y}")
            var clr = Mandelbrot(d)
            if(clr==1000){
                scope.drawCircle(Color.White, radius = 1f,
                    center = Offset(i.toFloat(),j.toFloat()))
            }
            else(scope.drawCircle(Color.Black, radius = 1f,
                center = Offset(i.toFloat(),j.toFloat())))
        }
    }
}
