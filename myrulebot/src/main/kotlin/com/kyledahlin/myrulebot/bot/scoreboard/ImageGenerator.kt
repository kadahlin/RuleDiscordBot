/*
*Copyright 2020 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.kyledahlin.myrulebot.bot.scoreboard

import com.kyledahlin.rulebot.bot.Logger
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel

/**
 * Generate dynamic scoreboard charts.
 *
 * The charts take the form of a 2D set of bars saved as pngs
 */

internal data class ChartPoint(val label: String, val value: Int)

private const val HEIGHT = 600
private const val WIDTH_SEGMENT = 200
private const val FILENAME = "scoreboard_chart.png"

private val FIRST_COLOR = Color.red
private val SECOND_COLOR = Color.blue
private val THIRD_COLOR = Color.green
private val OTHER_COLOR = Color.yellow

/**
 * Generate the chart for these wins and return the file location, null if could not be saved
 */
internal fun generateWinChart(points: Collection<ChartPoint>, title: String): String? {
    val dim = Dimension(
        WIDTH_SEGMENT * points.size,
        HEIGHT
    )
    val bc = BarChart(points, title).apply {
        size = dim
        minimumSize = dim
        maximumSize = dim
        preferredSize = dim
    }
    return bc.saveToImage()
}

private class BarChart(
    private val values: Collection<ChartPoint>,
    private val title: String
) : JPanel() {

    internal fun saveToImage(): String? {
        if (values.isEmpty() || values.all { it.value == 0 }) return null
        val bi = BufferedImage(this.size.width, this.size.height, BufferedImage.TYPE_INT_ARGB)
        val g = bi.createGraphics()
        this.paint(g)  //this == JComponent
        g.dispose()
        return try {
            ImageIO.write(bi, "png", File(FILENAME))
            FILENAME
        } catch (e: Exception) {
            Logger.logError("error on saving the bar chart")
            null
        }
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (values.isEmpty()) {
            return
        }

        val maxValue = values.maxBy { it.value }!!.value

        val dim = size
        val panelWidth = dim.width
        val panelHeight = dim.height
        val sectionWidth = panelWidth / values.size
        val barWidth = sectionWidth * (3 / 4f)

        val titleFont = Font("Comic Sans", Font.BOLD, 16)
        val titleFontMetrics = g.getFontMetrics(titleFont)

        val labelFont = Font("Comic Sans", Font.PLAIN, 14)
        val labelFontMetrics = g.getFontMetrics(labelFont)

        val titleWidth = titleFontMetrics.stringWidth(title)
        var stringHeight = titleFontMetrics.ascent
        var stringWidth = (panelWidth - titleWidth) / 2

        g.font = titleFont
        g.drawString(title, stringWidth, stringHeight)

        val top = titleFontMetrics.height + (titleFontMetrics.height)
        val bottom = labelFontMetrics.height + (labelFontMetrics.height / 3)

        val maxBarHeight = (panelHeight - top - bottom) / maxValue
        stringHeight = panelHeight - labelFontMetrics.descent
        g.font = labelFont

        values
            .sortedBy { it.label }
            .map {
                val newLabel = if (it.label.length <= 12) {
                    it.label
                } else {
                    it.label.substring(0..9).plus("...")
                }
                it.copy(label = newLabel)
            }
            .forEachIndexed { index, chartPoint ->
                var valueP = index * sectionWidth + 1f
                valueP += (sectionWidth - barWidth) / 2f
                var valueQ = top
                var height = (chartPoint.value * maxBarHeight)
                if (chartPoint.value >= 0) {
                    valueQ += ((maxValue - chartPoint.value) * maxBarHeight)
                } else {
                    valueQ += (maxValue * maxBarHeight)
                    height = -height
                }

                g.color = getColorForPosition(chartPoint, values)
                g.fillRect(valueP.toInt(), valueQ, (barWidth - 2).toInt(), height)
                g.color = Color.black
                g.drawRect(valueP.toInt(), valueQ, (barWidth - 2).toInt(), height)

                var labelWidth = labelFontMetrics.stringWidth(chartPoint.label)
                stringWidth = index * sectionWidth + (sectionWidth - labelWidth) / 2
                g.drawString(chartPoint.label, stringWidth, stringHeight)

                //draw the amount of wins above each bar
                labelWidth = labelFontMetrics.stringWidth(chartPoint.value.toString())
                stringWidth = index * sectionWidth + (sectionWidth - labelWidth) / 2
                g.drawString(chartPoint.value.toString(), stringWidth, valueQ - 3)
            }
    }

    private fun getColorForPosition(point: ChartPoint, points: Collection<ChartPoint>): Color {
        val playersAbove = points
            .filterNot { it == point }
            .filter { it.value > point.value }
            .size

        return when (playersAbove) {
            0    -> FIRST_COLOR
            1    -> SECOND_COLOR
            2    -> THIRD_COLOR
            else -> OTHER_COLOR
        }
    }
}
