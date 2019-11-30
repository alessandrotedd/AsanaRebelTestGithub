package app.alessandrotedesco.asana.github

/**
 * remove unnecessary double spaces and new lines
 */
fun String.withoutUnnecessaryCharacters(): String {
    // remove new lines and leading/trailing whitespaces
    var string = this.replace('\n', ' ').trim()
    var stringPreviousLength = string.length
    // while there might be something to compress
    while (true) {
        // reduce unnecessary spaces
        string = string.replace("  ", " ")
        // if nothing changed, stop compressing
        if (string.length == stringPreviousLength)
            break
        // otherwise, set the new previousLength
        stringPreviousLength = string.length
    }
    return string
}