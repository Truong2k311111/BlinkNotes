import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TreeNode(
    val id: String,
    val count: Int? = null,
    val children: List<TreeNode> = emptyList()
)

data class PositionedNode(
    val id: String,
    val count: Int?,
    val x: Float,
    val y: Float,
    val children: List<PositionedNode> = emptyList()
)

fun layoutTreeVertical(
    root: TreeNode,
    x: Float,
    y: Float,
    horizontalSpacing: Float = 150f,
    verticalSpacing: Float = 100f
): PositionedNode {
    val children = root.children
    if (children.isEmpty()) return PositionedNode(root.id, root.count, x, y)

    // Tính toán chiều rộng cần thiết để căn giữa các node con
    val subtreeWidths = children.map { subtreeWidth(it) }
    val totalWidth = subtreeWidths.sum() * horizontalSpacing
    var currentX = x - totalWidth / 2

    val positionedChildren = children.mapIndexed { index, child ->
        val childSubtreeWidth = subtreeWidths[index] * horizontalSpacing
        val childX = currentX + childSubtreeWidth / 2
        currentX += childSubtreeWidth

        layoutTreeVertical(
            child,
            childX,
            y + verticalSpacing,
            horizontalSpacing,
            verticalSpacing
        )
    }

    return PositionedNode(root.id, root.count, x, y, positionedChildren)
}

fun subtreeWidth(node: TreeNode): Int {
    if (node.children.isEmpty()) return 1
    return node.children.sumOf { subtreeWidth(it) }
}

@Composable
fun DrawCombinedTree(root: TreeNode) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val startX = size.width / 2f
        val startY = 80f
        val positionedRoot = layoutTreeVertical(root, startX, startY)

        fun drawNode(node: PositionedNode) {
            // Vẽ hình tròn node
            drawCircle(
                color = Color.Blue,
                radius = 20f,
                center = Offset(node.x, node.y)
            )

            // Vẽ text
            val displayText = if (node.count != null) "${node.id} (${node.count}) ↔" else node.id
            val textLayoutResult = textMeasurer.measure(displayText)
            drawText(
                textLayoutResult,
                topLeft = Offset(node.x - textLayoutResult.size.width / 2, node.y - 30f)
            )

            // Vẽ đường kết nối
            node.children.forEach { child ->
                // Đường thẳng đứng xuống từ node cha
                drawLine(
                    color = Color.Gray,
                    start = Offset(node.x, node.y + 20f),
                    end = Offset(node.x, child.y - 20f),
                    strokeWidth = 2f
                )

                // Đường ngang nối vào node con (nếu cần)
                if (child.x != node.x) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(node.x, child.y),
                        end = Offset(child.x, child.y),
                        strokeWidth = 2f
                    )
                }

                drawNode(child)
            }
        }

        drawNode(positionedRoot)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CombinedTreePreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            val tree = TreeNode(
                id = "Modern Chinh",
                children = listOf(
                    TreeNode("", 6, listOf(
                        TreeNode("()", children = listOf(
                            TreeNode(""),
                            TreeNode("")
                        ))
                    )),
                    TreeNode("", 4, listOf(
                        TreeNode("()", children = listOf(
                            TreeNode(""),
                            TreeNode("")
                        )),
                        TreeNode("", 4)
                    )),
                    TreeNode("", 2),
                    TreeNode("", 2)
                )
            )
            DrawCombinedTree(tree)
        }
    }
}