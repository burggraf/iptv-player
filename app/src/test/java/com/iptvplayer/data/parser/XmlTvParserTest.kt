package com.iptvplayer.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class XmlTvParserTest {

    private val parser = XmlTvParser()

    @Test
    fun `parse XMLTV channel and programme`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv generator-info-name="test">
              <channel id="BBC1.uk">
                <display-name lang="en">BBC One</display-name>
                <icon src="https://example.com/bbc1.png"/>
              </channel>
              <programme start="20240101120000 +0000" stop="20240101123000 +0000" channel="BBC1.uk">
                <title lang="en">News at Twelve</title>
                <desc lang="en">The latest news</desc>
                <category lang="en">News</category>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.channels.size)
        assertEquals("BBC One", result.channels[0].displayName)
        assertEquals("https://example.com/bbc1.png", result.channels[0].iconUrl)
        assertEquals(1, result.channels[0].programmes.size)
        assertEquals("News at Twelve", result.channels[0].programmes[0].title)
        assertEquals("The latest news", result.channels[0].programmes[0].description)
        assertEquals("News", result.channels[0].programmes[0].category)
    }

    @Test
    fun `parse multiple channels with programmes`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv>
              <channel id="BBC1.uk"><display-name>BBC One</display-name></channel>
              <channel id="BBC2.uk"><display-name>BBC Two</display-name></channel>
              <programme start="20240101180000 +0000" stop="20240101183000 +0000" channel="BBC1.uk">
                <title>News</title>
              </programme>
              <programme start="20240101183000 +0000" stop="20240101190000 +0000" channel="BBC1.uk">
                <title>Sport</title>
              </programme>
              <programme start="20240101180000 +0000" stop="20240101190000 +0000" channel="BBC2.uk">
                <title>Documentary</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(2, result.channels.size)
        val bbc1 = result.channels.find { it.id == "BBC1.uk" }!!
        assertEquals(2, bbc1.programmes.size)
        assertEquals("News", bbc1.programmes[0].title)
        assertEquals("Sport", bbc1.programmes[1].title)

        val bbc2 = result.channels.find { it.id == "BBC2.uk" }!!
        assertEquals(1, bbc2.programmes.size)
        assertEquals("Documentary", bbc2.programmes[0].title)
    }

    @Test
    fun `parse dates correctly`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv>
              <channel id="T1"><display-name>Test</display-name></channel>
              <programme start="20240101120000 +0000" stop="20240101123000 +0000" channel="T1">
                <title>Show</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)
        val programme = result.channels[0].programmes[0]

        assertEquals(
            Instant.parse("2024-01-01T12:00:00Z"),
            programme.startAt
        )
        assertEquals(
            Instant.parse("2024-01-01T12:30:00Z"),
            programme.endAt
        )
    }

    @Test
    fun `parse handles optional fields`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv>
              <channel id="T1"><display-name>Test</display-name></channel>
              <programme start="20240101120000 +0000" stop="20240101123000 +0000" channel="T1">
                <title>Minimal</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)
        val programme = result.channels[0].programmes[0]

        assertEquals("Minimal", programme.title)
        assertTrue(programme.description == null || programme.description!!.isEmpty())
    }

    @Test
    fun `parse returns empty result for empty xml`() {
        val xml = "<?xml version=\"1.0\"?><tv></tv>"
        val result = parser.parse(xml)
        assertTrue(result.channels.isEmpty())
    }
}
