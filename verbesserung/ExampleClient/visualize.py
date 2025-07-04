import argparse

import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
from matplotlib.widgets import Button


class GameVisualizer:
    def __init__(self, states, interval=1000):
        self.states = states
        self.n_states = len(states)
        self.idx = 0
        self.playing = True
        self.interval = interval

        # Define mapping from characters to numeric codes and colors
        self.code_map = {'G': 0, 'M': 1, 'W': 2, 'P': 3, 'E': 4, 'F': 5, '.': 6}
        self.colors = ['lightgreen', 'dimgray', 'dodgerblue', 'gold', 'red', 'brown', 'white']
        self.cmap = mcolors.ListedColormap(self.colors)

        # Set up figure and axis
        self.fig, self.ax = plt.subplots()
        plt.subplots_adjust(bottom=0.2)
        self.ax.axis('off')

        # Initial draw
        self.grid_im = None
        self.texts = []
        self._draw_state()

        # Buttons
        ax_prev = plt.axes([0.1, 0.05, 0.1, 0.075])
        ax_play = plt.axes([0.3, 0.05, 0.1, 0.075])
        ax_next = plt.axes([0.5, 0.05, 0.1, 0.075])

        self.btn_prev = Button(ax_prev, 'Prev')
        self.btn_play = Button(ax_play, 'Pause')
        self.btn_next = Button(ax_next, 'Next')

        self.btn_prev.on_clicked(self.on_prev)
        self.btn_play.on_clicked(self.on_play_pause)
        self.btn_next.on_clicked(self.on_next)

        # Timer for auto-advance
        self.timer = self.fig.canvas.new_timer(interval=self.interval)
        self.timer.add_callback(self.next_frame)
        self.timer.start()

    def _draw_state(self):
        state = self.states[self.idx]
        nrows = len(state)
        ncols = len(state[0]) if nrows > 0 else 0

        # Build numeric grid and overlay letters
        num_grid = []
        for row in state:
            code_row = [self.code_map.get(ch.upper(), 6) for ch in row]
            num_grid.append(code_row)

        # Draw or update image
        if self.grid_im is None:
            self.grid_im = self.ax.imshow(num_grid, cmap=self.cmap, vmin=0, vmax=len(self.colors)-1, interpolation='none')
            # Add text overlays
            for i, row in enumerate(state):
                text_row = []
                for j, ch in enumerate(row):
                    txt = self.ax.text(j, i, ch, ha='center', va='center', fontsize=8)
                    text_row.append(txt)
                self.texts.append(text_row)
        else:
            self.grid_im.set_data(num_grid)
            for i, row in enumerate(state):
                for j, ch in enumerate(row):
                    self.texts[i][j].set_text(ch)

        self.ax.set_title(f"Step {self.idx + 1} / {self.n_states}")
        self.fig.canvas.draw_idle()

    def next_frame(self):
        if self.playing:
            self.idx = (self.idx + 1) % self.n_states
            self._draw_state()

    def on_play_pause(self, event):
        self.playing = not self.playing
        if self.playing:
            self.btn_play.label.set_text('Pause')
            self.timer.start()
        else:
            self.btn_play.label.set_text('Play')
            self.timer.stop()

    def on_next(self, event):
        self.idx = (self.idx + 1) % self.n_states
        self._draw_state()

    def on_prev(self, event):
        self.idx = (self.idx - 1) % self.n_states
        self._draw_state()


def parse_states(filename):
    states = []
    current = []
    with open(filename, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n')
            # Skip separators and timestamps
            if line.startswith('=====') or line.startswith('🕒') or not line:
                if current:
                    states.append(current)
                    current = []
                continue
            current.append(line)
        if current:
            states.append(current)
    return states


def main():
    parser = argparse.ArgumentParser(description='Visualize gridworld game states')
    parser.add_argument('file', help='Log file with game states')
    parser.add_argument('--delay', type=int, default=1000, help='Delay between steps in ms')
    args = parser.parse_args()

    states = parse_states(args.file)
    if not states:
        print('No states found in file.')
        return

    vis = GameVisualizer(states, interval=args.delay)
    plt.show()

if __name__ == '__main__':
    main()
