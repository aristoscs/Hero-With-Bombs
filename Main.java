import java.util.*;

class Bomb {
    int x;
    int y;

    int tick = 3;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Bomb(Bomb otherBomb) {
        this.x = otherBomb.x;
        this.y = otherBomb.y;
        this.tick = otherBomb.tick - 1;
    }
}

class Node implements Comparable<Node> {

    Node parent;

    char[][] maze;

    int g;
    int h;

    int x;
    int y;

    char direction;

    Bomb bomb;

    List<Node> children() {
        List<Node> children = new ArrayList<>();
        if (!exis('T') || !exis('E'))
            return children;

        // UP, DOWN, LEFT, RIGHT, BOMB, WAIT if we have bomb currently
        if (x - 1 >= 0 && maze[x - 1][y] != 'X') {
            Node up = new Node(this, maze, g + 1, x - 1, y, 'U', bomb == null ? null : new Bomb(bomb));

            char temp = up.maze[x][y];
            up.maze[x][y] = up.maze[x - 1][y];
            up.maze[x - 1][y] = temp;

            children.add(up);
        }

        if (x + 1 < maze.length && maze[x + 1][y] != 'X') {
            Node down = new Node(this, maze, g + 1, x + 1, y, 'D', bomb == null ? null : new Bomb(bomb));

            char temp = down.maze[x][y];
            down.maze[x][y] = down.maze[x + 1][y];
            down.maze[x + 1][y] = temp;

            children.add(down);
        }

        if (y - 1 >= 0 && maze[x][y - 1] != 'X') {
            Node left = new Node(this, maze, g + 1, x, y - 1, 'L', bomb == null ? null : new Bomb(bomb));

            char temp = left.maze[x][y];
            left.maze[x][y] = left.maze[x][y - 1];
            left.maze[x][y - 1] = temp;

            children.add(left);
        }

        if (y + 1 < maze[x].length && maze[x][y + 1] != 'X') {
            Node right = new Node(this, maze, g + 1, x, y + 1, 'R', bomb == null ? null : new Bomb(bomb));

            char temp = right.maze[x][y];
            right.maze[x][y] = right.maze[x][y + 1];
            right.maze[x][y + 1] = temp;

            children.add(right);
        }

        // Bomb
        if (this.bomb == null) {
            children.add(new Node(this, maze, g + 1, x, y, 'M', new Bomb(x, y)));
        }

        // Wait
        if (this.bomb != null && this.bomb.tick < 2) {
            children.add(new Node(this, maze, g + 1, x, y, 'T', new Bomb(this.bomb)));
        }

        return children;
    }

    private boolean exis(char c) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == c)
                    return true;
            }
        }
        return false;
    }

    public Node(Node parent, char[][] maze, int g, int heroX, int heroY, char direction, Bomb bomb) {
        this.parent = parent;

        this.maze = new char[maze.length][maze[0].length];
        for (int i = 0; i < maze.length; i++) {
            System.arraycopy(maze[i], 0, this.maze[i], 0, maze[i].length);
        }

        this.g = g;
        this.h = Math.abs(heroX - Main.exitX) + Math.abs(heroY - Main.exitY);

        this.x = heroX;
        this.y = heroY;

        this.direction = direction;

        this.bomb = bomb;
    }

    @Override
    public int compareTo(Node o) {
        int f1 = this.g + this.h;
        int f2 = o.g + o.h;

        if (f1 == f2)
            return o.g - this.g;
        return f1 - f2;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                string.append(maze[i][j]);
            }
        }
        return string.toString();
    }

    public void checkBomb() {
        if (bomb != null) {
            if (bomb.tick == 0) {
                int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
                int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

                // Explode cells around the bomb
                for (int i = 0; i < dx.length; i++) {
                    int newRow = bomb.x + dx[i];
                    int newCol = bomb.y + dy[i];
                    if (newRow >= 0 && newRow < maze.length && newCol >= 0 && newCol < maze[0].length) {
                        maze[newRow][newCol] = '0';
                    }
                }
                maze[bomb.x][bomb.y] = '0';
                bomb = null;
            }
        }
    }

}

class Main {

    static char[][] grid = {
            {'X', 'E', 'X', 'X', 'X'},
            {'X', '.', 'X', 'X', 'T'},
            {'X', '.', 'X', 'X', 'X'},
    };


    static int heroX, heroY;
    static int exitX, exitY;

    static {
        findHero();
        findExit();
        printGrid();
    }

    public static void printGrid() {
        for (char[] chars : grid) {
            System.out.println(Arrays.toString(chars));
        }
    }

    public static void findHero() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 'E') {
                    heroX = i;
                    heroY = j;
                    return;
                }
            }
        }
    }

    public static void findExit() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 'T') {
                    exitX = i;
                    exitY = j;
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        Node solution = a_star();
        if (solution == null)
            System.out.println("No Solution.");
        else {
            System.out.println(getSequenceOf(solution));
        }
    }

    private static String getSequenceOf(Node solution) {
        StringBuilder sequence = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        while (solution.parent != null) {
            stack.add(solution.direction);
            solution = solution.parent;
        }

        while (!stack.isEmpty()) {
            sequence.append(stack.pop());
        }
        return sequence.toString();
    }

    private static Node a_star() {
        PriorityQueue<Node> open = new PriorityQueue<>();
        HashSet<String> visited = new HashSet<>();

        open.add(new Node(null, grid, 0, heroX, heroY, '\0', null));
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.x == exitX && current.y == exitY) {
                return current;
            }

            current.checkBomb();

            for (Node c : current.children()) {
                if (c.bomb != null)
                    open.add(c);
                else if (!visited.contains(c.toString()))
                    open.add(c);
            }
            visited.add(current.toString());
        }
        return null;
    }
}
