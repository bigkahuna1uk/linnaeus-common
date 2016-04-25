package com.springer.rejectiontracker

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.springer.rejectionreport.objectMapper
import com.springer.rejectiontracker.Imprint.BIOMED_CENTRAL
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

class RejectedManuscriptJsonSerialisationTest {
    val mapper = objectMapper

    @Test
    fun can_deserialise_untracked_manuscript() {
        assertThat(mapper.readValue<RejectedManuscript>(untrackedJson) as UntrackedManuscript,
            equalTo(untrackedManuscript))
    }

    @Test
    fun can_deserialise_tracked_manuscript() {
        assertThat(mapper.readValue<RejectedManuscript>(trackedJson) as TrackedManuscript,
            equalTo(trackedManuscript))
    }

    @Test
    fun can_serialise_tracked_manuscript() {
        val actual = mapper.valueToTree<JsonNode>(trackedManuscript)
        val expected = mapper.readTree(trackedJson)

        assertEquals(
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected),
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual))
    }

    private val untrackedJson = """{
        "tracked": false,
        "source": "BIOMED_CENTRAL",
        "manuscriptId": "3000514641226990",
        "rejectedFromJournalId": "2037",
        "rejectedFromJournalIssn": "1234-5678",
        "rejectedFromJournalName": "BMC Health Services Research",
        "dateRejected": "2015-04-21",
        "dateSubmitted": "2015-04-21",
        "transferred": false,
        "title": "The elderly in Thailand: a systematic review",
        "authors": [
            "Pattaraporn Khongboon",
            "Sathirakorn Pongpanich",
            "Viroj Tangcharoensatien"
        ],
        "publishedBy": null,
        "titleAfterPublication": null,
        "url": null,
        "doi": null,
        "publishedInJournalIssn": null,
        "publishedInJournalName": null
    }"""

    private val trackedJson = """{
        "tracked": true,
        "source": "BIOMED_CENTRAL",
        "manuscriptId": "2947243881235387",
        "rejectedFromJournalId": "10147",
        "rejectedFromJournalIssn": "1234-5678",
        "rejectedFromJournalName": "Journal of Cardiovascular Magnetic Resonance",
        "dateRejected": "2015-04-21",
        "dateSubmitted": "2015-04-21",
        "dateLastChecked" : "2016-01-01T12:00:00Z",
        "transferred": false,
        "title": "Giant left ventricular aneurysm",
        "authors": [
            "Gustavo Schaitza",
            "José Rocha Faria Neto",
            "Leanderson Franco de Meira"
        ],
        "publishedBy": "AVES Publishing Co.",
        "titleAfterPublication": "Giant Left Ventricular Aneurysm",
        "url": "http://dx.doi.org/10.5152/ejp.2014.04127",
        "doi": "10.5152/ejp.2014.04127",
        "publishedInJournalIssn": "2148-3620",
        "publishedInJournalName": "Eurasian Journal of Pulmonology",
        "issuedDate": "2014-09-02"
    }"""

    private val trackedManuscript = TrackedManuscript(
        source = BIOMED_CENTRAL,
        manuscriptId = "2947243881235387",
        rejectedFromJournalId = "10147",
        rejectedFromJournalIssn= "1234-5678",
        rejectedFromJournalName = "Journal of Cardiovascular Magnetic Resonance",
        rejectedDate = LocalDate.parse("2015-04-21"),
        submittedDate = LocalDate.parse("2015-04-21"),
        lastCheckedTime = ZonedDateTime.parse("2016-01-01T12:00Z[UTC]"),
        title = "Giant left ventricular aneurysm",
        authors = listOf(
            "Gustavo Schaitza",
            "José Rocha Faria Neto",
            "Leanderson Franco de Meira"
        ),
        publishedBy = "AVES Publishing Co.",
        titleAfterPublication = "Giant Left Ventricular Aneurysm",
        url = "http://dx.doi.org/10.5152/ejp.2014.04127",
        doi = "10.5152/ejp.2014.04127",
        publishedInJournalIssn = "2148-3620",
        publishedInJournalName = "Eurasian Journal of Pulmonology",
        issuedDate = LocalDate.of(2014, 9, 2), // NB - this wouldn't be tracked now we consider publication dates
        transferred=false
    )

    private val untrackedManuscript = UntrackedManuscript(
        source = BIOMED_CENTRAL,
        manuscriptId = "3000514641226990",
        rejectedFromJournalId = "2037",
        rejectedFromJournalIssn = "1234-5678",
        rejectedFromJournalName = "BMC Health Services Research",
        rejectedDate = LocalDate.parse("2015-04-21"),
        submittedDate = LocalDate.parse("2015-04-21"),
        lastCheckedTime = null,
        transferred = false,
        title = "The elderly in Thailand: a systematic review",
        authors = listOf(
            "Pattaraporn Khongboon",
            "Sathirakorn Pongpanich",
            "Viroj Tangcharoensatien"
        )
    )
}