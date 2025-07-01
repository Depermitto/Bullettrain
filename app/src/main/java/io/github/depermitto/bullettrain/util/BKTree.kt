package io.github.depermitto.bullettrain.util

import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.Stack
import kotlin.math.abs

class BKTree {
    data class Node(
        val word: String,
        val children: MutableMap<Int, Node> = mutableMapOf(),
    )

    private lateinit var root: Node

    /**
     * Insert [key] in to the [BKTree].
     *
     * @return false if inserted, true if the [key] already existed in the tree.
     */
    fun insert(key: String): Boolean {
        if (!::root.isInitialized) {
            root = Node(key)
            return false
        }

        var node: Node? = root
        while (node != null) {
            val distance = LevenshteinDistance().apply(node.word, key)
            if (distance == 0) return true

            node = node.children.putIfAbsent(distance, Node(key))
        }
        return false
    }

    /**
     * Search the [BKTree] for [key].
     *
     * @param errorTolerance maximum acceptable amount of insertions/substitutions/deletions
     * between [key] and any element in the tree.
     * @param ignoreCase whether to include case sensitivity in the [errorTolerance] or not.
     *
     * @return closest element in the tree to the [key] with regards to [errorTolerance] and [ignoreCase]
     * or null if none is eligible.
     */
    fun search(key: String, errorTolerance: Int = 5, ignoreCase: Boolean = false): String? {
        var closest: String? = null
        var distanceBest = errorTolerance + 1

        val nodes = Stack<Node>().apply { push(root) }
        while (nodes.isNotEmpty()) {
            val node = nodes.pop()
            val distance = if (ignoreCase) {
                LevenshteinDistance().apply(node.word.lowercase(), key.lowercase())
            } else {
                LevenshteinDistance().apply(node.word, key)
            }

            if (distance < distanceBest) {
                closest = node.word
                distanceBest = distance
            }

            for ((childDistance, childNode) in node.children) {
                if (abs(childDistance - distance) < distanceBest) nodes.push(childNode)
            }
        }

        return closest
    }

    fun delete(key: String) {
        TODO("Maybe add a delete function once user deletes and exercise.")
    }
}