package com.example.bomberman.entities;
import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

import java.util.*;

public class Oneal extends Enemy {

    private static final double ONEAL_SPEED = 75.0; // Tốc độ của Oneal (nhanh hơn Balloom?)
    private static final int ONEAL_SCORE = 200;    // Điểm của Oneal
    private static final double BFS_UPDATE_INTERVAL = 1.0; // Chạy BFS mỗi 0.5 giây (điều chỉnh)

    private double bfsTimer = 0; // Timer để kiểm soát tần suất chạy BFS
    private Queue<Direction> currentPath = new LinkedList<>(); // Hàng đợi lưu đường đi (hoặc chỉ cần hướng tiếp theo)
    private Direction nextBFSDirection = Direction.NONE; // Hướng đi được tính bởi BFS

    // Lớp nội bộ để lưu trữ Node cho BFS
    private static class Node {
        int x, y;
        Direction cameFromDirection; // Hướng đi để đến được node này TỪ node cha
        Node parent; // Lưu node cha để dò lại đường đi (nếu cần đầy đủ đường đi)

        Node(int x, int y, Direction dir, Node parent) {
            this.x = x;
            this.y = y;
            this.cameFromDirection = dir;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }


    public Oneal(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        super(startGridX, startGridY, ONEAL_SPEED, ONEAL_SCORE, map, gameManager);

        // --- Khởi tạo Animations cho Oneal ---
        double frameDuration = 0.2; // Thời gian frame (điều chỉnh)
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.oneal_left1, Sprite.oneal_left2, Sprite.oneal_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.oneal_right1, Sprite.oneal_right2, Sprite.oneal_right3);
        // Animation chết dùng chung mob_dead hoặc oneal_dead
        deadAnimation = new Animation(TIME_TO_DIE / 3.0, false, Sprite.oneal_dead, Sprite.mob_dead1, Sprite.mob_dead2, Sprite.mob_dead3);

        // Animation ban đầu
        currentAnimation = walkLeftAnimation; // Hoặc walkRightAnimation tùy hướng ban đầu
    }

    // --- Override phương thức tính toán nước đi ---
    @Override
    protected void calculateNextMove() {
        // Chỉ tính toán lại đường đi bằng BFS sau một khoảng thời gian nhất định
        if (bfsTimer <= 0) {
            findPathToPlayer(); // Chạy BFS để tìm hướng mới
            bfsTimer = BFS_UPDATE_INTERVAL; // Reset timer
        }

        // Luôn đặt hướng hiện tại theo kết quả BFS gần nhất
        if (nextBFSDirection != Direction.NONE) {
            currentDirection = nextBFSDirection;
            isMoving = true;
            // Cập nhật animation dựa trên hướng BFS
            if (nextBFSDirection != Direction.NONE) {
                currentDirection = nextBFSDirection;
                isMoving = true;
                // Cập nhật animation nếu cần (có thể đưa vào hàm riêng hoặc move())
                updateAnimationForDirection(currentDirection); // Gọi hàm helper
            } else {
                // << SỬA Ở ĐÂY >>
                // Nếu BFS không tìm thấy đường, HOẶC nếu hướng hiện tại là NONE (vừa bị chặn xong)
                // thì chọn hướng ngẫu nhiên thông minh
                if (!isMoving || currentDirection == Direction.NONE) {
                    setRandomDirection(); // Gọi hàm setRandomDirection() của lớp Enemy (sẽ được cải tiến)
                }
            }
        }
    }

    @Override
    public void update(double deltaTime) {
        if (!isAlive) {
            super.update(deltaTime); // Xử lý animation chết
            return;
        }
        // Giảm timer BFS
        bfsTimer -= deltaTime;
        // Gọi update của lớp cha (bao gồm calculateNextMove và move)
        super.update(deltaTime);
    }


    // --- Thuật toán BFS để tìm đường đến Player ---
    private void findPathToPlayer() {
        nextBFSDirection = Direction.NONE; // Reset hướng trước khi tìm mới
        Player player = gameManager.getPlayer();
        if (player == null || !player.isAlive()) {
            //System.out.println("Oneal BFS: Player not found or dead.");
            return; // Không tìm đường nếu không có Player hoặc Player đã chết
        }

        int targetX = player.getGridX();
        int targetY = player.getGridY();

        // Hàng đợi cho BFS
        Queue<Node> queue = new LinkedList<>();
        // Set để lưu các ô đã thăm (dùng String "x,y" làm key)
        Set<String> visited = new HashSet<>();
        // Map để lưu node cha (nếu cần dò lại cả đường đi) - Tạm thời không cần thiết nếu chỉ lấy bước đầu
        // Map<Node, Node> parentMap = new HashMap<>();

        // Bắt đầu từ vị trí hiện tại của Oneal
        Node startNode = new Node(this.gridX, this.gridY, Direction.NONE, null);
        queue.add(startNode);
        visited.add(startNode.x + "," + startNode.y);

        Node targetNodeFound = null;

        // Mảng tiện ích cho việc duyệt các ô lân cận (Lên, Xuống, Trái, Phải)
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();

            // Nếu tìm thấy Player
            if (currentNode.x == targetX && currentNode.y == targetY) {
                targetNodeFound = currentNode;
                //System.out.println("Oneal BFS: Path found!");
                break; // Thoát vòng lặp vì đã đến đích
            }

            // Duyệt các ô lân cận
            for (int i = 0; i < 4; i++) {
                int nextX = currentNode.x + dx[i];
                int nextY = currentNode.y + dy[i];
                Direction directionToNext = directions[i];
                String nextKey = nextX + "," + nextY;

                // Kiểm tra xem ô lân cận có hợp lệ và chưa được thăm không
                if (isValidAndWalkable(nextX, nextY) && !visited.contains(nextKey)) {
                    visited.add(nextKey);
                    Node neighborNode = new Node(nextX, nextY, directionToNext, currentNode); // Lưu node cha và hướng đi
                    queue.add(neighborNode);
                    // parentMap.put(neighborNode, currentNode); // Nếu cần lưu cha
                }
            }
        }

        // --- Lấy hướng đi đầu tiên từ đường đi đã tìm được ---
        if (targetNodeFound != null) {
            Node stepNode = targetNodeFound;
            // Dò ngược về node ngay sau node bắt đầu để tìm hướng đi đầu tiên
            while (stepNode.parent != null && stepNode.parent != startNode) {
                stepNode = stepNode.parent;
            }
            // stepNode bây giờ là node đầu tiên sau startNode trên đường đi ngắn nhất
            if (stepNode != startNode) { // Đảm bảo target không phải là start
                nextBFSDirection = stepNode.cameFromDirection; // Lấy hướng đã dùng để ĐẾN được node này
                //System.out.println("Oneal BFS: Next move direction: " + nextBFSDirection);
            } else {
                //System.out.println("Oneal BFS: Target is the start node?");
                nextBFSDirection = Direction.NONE; // Đứng yên nếu đang ở trên Player
            }
        } else {
            //System.out.println("Oneal BFS: No path found to player.");
            nextBFSDirection = Direction.NONE; // Không tìm thấy đường
        }
    }

    // --- Helper kiểm tra ô hợp lệ và có thể đi qua cho BFS ---
    private boolean isValidAndWalkable(int gX, int gY) {
        // Dùng lại hàm isObstacle nhưng đảo ngược kết quả
        // isObstacle trả về true NẾU LÀ vật cản
        return !isObstacle(gX, gY);
    }
    public void clearPath() {
        currentPath.clear(); // clear() xóa tất cả phần tử khỏi Queue
    }
    public void consumeNextPathDirection() {
        if (!currentPath.isEmpty()) {
            currentPath.poll(); // poll() lấy và xóa phần tử đầu tiên
        }
    }
    public boolean isPathEmpty() {
        return currentPath.isEmpty();
    }
    public Direction peekNextPathDirection() {
        if (!currentPath.isEmpty()) {
            return currentPath.peek(); // peek() lấy phần tử đầu tiên mà không xóa
        }
        return Direction.NONE; // Trả về NONE nếu không có path
    }


}