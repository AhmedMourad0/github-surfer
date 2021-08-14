package dev.ahmedmourad.githubsurfer.common

/**
 * This interface is to be implemented by both the search results fragment and
 * any fragment that can trigger a search event
 * - The search results fragment should update its results based on the new query
 * - Other fragments should start the search results fragment
 */
interface SearchHandler {
    fun onSearch(query: String?)
}
