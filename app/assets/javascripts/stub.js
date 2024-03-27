function populateTextArea(journeyData) {
    document.getElementById("grsJourneyDataJson").value = JSON.stringify(journeyData, null, 2)
}
