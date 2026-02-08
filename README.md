# Bitmap to ASCII Art for Android

A simple Bitmap image converter to ASCII art on Kotlin. The function accepts a `Bitmap`

## Features

- Conversion of any `Bitmap` into ASCII text art.
- Control of the output width (in characters).
- Customizable character set from dark to light.
- Suitable for Android projects (uses `android.graphics.Bitmap`).

## Example

```
                                                  =:              .:--::.                               
                                                . .#+:.............:-=+=:=:                             
                                                   .+#+---------==---+-=-=+*=..                         
                                               .   ...-=+=========++*###*+*###=.                        
                                                  ...:::=#+:.:.:-----==**%#%#+-:.                       
                                                  ...:....=*-.    .....-+##*##*=::..                    
                                                 ....:..   .++..... .....-++++**=:::.                   
                                                ....:..      .*:...::.::::-++*=+***+++=                 
                                               .:.....       .:            ..:::::....:                 
                                           =-..::....        .             ..:--:..  .                  
                                         ..%*+=-::...       ..            ..:-+=:..                     
                                         .%%#**==+-..       .             .:-+#+:....                   
                                         .+%*%%***+=:..    .             ..:+##=:...                    
                                          .-**+%#%##*=:.  .             ..:+#%+-:..                     
                                           ..=%##*###*+-.-.            ..:=#%#=:..                      
                                             ..#+-:*#*=--#.     .......::-*%%+=::.                      
                                               .==::-++=#-....:--=+*+*#%%#++-.                      
                                                 .-:::+%%-::::---===-=*%%%*#%%%%:.                      
                                                   .::+%:.... .......:+%%%#*+#*:.                       
                                                     :%:..   ..:---=++###+--=...                        
```

## How it works

1. Image scaling  
   `Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter)` is used to create a scaled-down copy of the source with the desired width and calculated height to maintain proportions.
2. Traversing all pixels  
   Two nested loops run along the `x` and `y` coordinates from `0` to the `width/height` of the reduced `Bitmap'.

3. Getting the pixel color  
   For each pixel, `GetPixel(x, y)` is called, then the components `r`, `g`, `b` (red, green, blue) are extracted from the integer ARGB value using bit operations.
4. Brightness calculation (grayscale)  
   The brightness is calculated using the formula  
   `gray = 0.299 * r + 0.587 * g + 0.114 * b`,
where `r`, `g`, `b` are values from 0 to 255.
5. Converting brightness to symbol  
   The brightness is normalized to the range from 0 to 1 and multiplied by the index of the last character in the string `chars'. A symbol is selected based on the resulting index: the darkest pixels turn into "dense" symbols. (`@`, `%`, `#`), and the lightest ones are in dots or spaces.

6. Formation of lines  
   For each row of pixels, a sequence of characters is collected and a newline is added. In the end, you get one big `String` with ASCII art.

## Function code

```kotlin
fun bitmapToAscii(
        bitmap: Bitmap,
        width: Int = 80,
        chars: String = "@%#*+=-:. "
    ): String {
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            width,
            (bitmap.height * width / bitmap.width.toFloat() / 2).toInt(),
            true
        )
        val sb = StringBuilder()
        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                val idx = ((gray / 255.0) * (chars.lastIndex)).toInt()
                sb.append(chars[idx])
            }
            sb.append('\n')
        }
        return sb.toString()
    }
