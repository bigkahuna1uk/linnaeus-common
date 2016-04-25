package com.springer.rejectiontracker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.ZonedDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "tracked")
@JsonSubTypes(Type(UntrackedManuscript::class, name = "false"), Type(TrackedManuscript::class, name = "true"))
interface RejectedManuscript {
    val source: Imprint
    val manuscriptId: String
    val rejectedFromJournalId : String
    val rejectedFromJournalIssn: String?
    val rejectedFromJournalName : String
    val rejectedDate: LocalDate @JsonProperty("dateRejected") get
    val submittedDate: LocalDate @JsonProperty("dateSubmitted") get
    val lastCheckedTime: ZonedDateTime? @JsonProperty("dateLastChecked") get
    val transferred : Boolean
    val title: String
    val authors: List<String>
    val tracked: Boolean

    fun copy(title: String, authors: List<String>): RejectedManuscript

    fun untracked(): UntrackedManuscript
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UntrackedManuscript(
    override val source: Imprint,
    override val manuscriptId: String,
    override val rejectedFromJournalId : String,
    override val rejectedFromJournalIssn: String?,
    override val rejectedFromJournalName : String,
    override val rejectedDate: LocalDate,
    override val submittedDate: LocalDate,
    override val lastCheckedTime: ZonedDateTime?,
    override val transferred : Boolean,
    override val title: String,
    override val authors: List<String>
) :
    RejectedManuscript
{
    override val tracked = false

    override fun copy(title: String, authors: List<String>) =
        this.copy(manuscriptId=manuscriptId, title=title, authors = authors)

    override fun untracked() = this
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrackedManuscript(
    override val source: Imprint,
    override val manuscriptId: String,
    override val rejectedFromJournalId : String,
    override val rejectedFromJournalIssn: String?,
    override val rejectedFromJournalName : String,
    override val rejectedDate: LocalDate,
    override val submittedDate: LocalDate,
    override val lastCheckedTime: ZonedDateTime,
    override val transferred : Boolean,
    override val title: String,
    override val authors: List<String>,
    val publishedBy: String?,
    val titleAfterPublication: String,
    val url: String,
    val doi: String,
    val publishedInJournalIssn: String?,
    val publishedInJournalName: String?,
    val issuedDate: LocalDate?
) :
    RejectedManuscript
{
    override val tracked = true

    override fun copy(title: String, authors: List<String>) =
        this.copy(manuscriptId=manuscriptId, title=title, authors = authors)

    override fun untracked() = UntrackedManuscript(
        source = source,
        manuscriptId = manuscriptId,
        rejectedFromJournalId = rejectedFromJournalId,
        rejectedFromJournalIssn = rejectedFromJournalIssn,
        rejectedFromJournalName = rejectedFromJournalName,
        rejectedDate = rejectedDate,
        submittedDate = submittedDate,
        transferred = transferred,
        title = title,
        authors = authors,
        lastCheckedTime = null
    )

}
