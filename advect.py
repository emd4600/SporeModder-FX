import matplotlib.pyplot as plt
import numpy as np
import struct
import argparse


def visualize_advect(input_path: str):
    with open(input_path, mode='rb') as f:
        data = f.read()
        vectors = [struct.unpack('<fff', data[i:i+12]) for i in range(0, 128*128*12, 12)]

    # Create a new figure and axis
    ax = plt.figure(figsize=(20, 20)).add_subplot()

    # Set the aspect ratio to 'equal' to maintain proportions
    ax.set_aspect('equal')

    x, y = np.meshgrid(np.arange(0, 128), np.arange(0, 128))

    ax.quiver(x[::2], y[::2], [v[0] for v in vectors][::2], [v[1] for v in vectors][::2])
    plt.show()

    # Add labels and title
    ax.set_xlabel('X')
    ax.set_ylabel('Y')
    ax.set_title('Advect Vectors')

    # Show the plot
    plt.show()


def generate_advect(output_path: str, math_expression: str):
    math_function = eval(f'lambda x, y: ({math_expression})')

    x, y = np.meshgrid(np.linspace(0.0, 1.0, num=128), np.linspace(0.0, 1.0, num=128))

    vectors = []
    for i in range(128):
        for j in range(128):
            values = math_function(x[i, j], y[i, j])
            if len(values) < 2 or len(values) > 3:
                raise ValueError('The mathematical expression must generate 2 or 3 numbers')
            if len(values) == 2:
                values = (values[0], values[1], 0.0)
            vectors.append(values)
      
    with open(output_path, mode='wb') as f:
        f.write(bytes().join(struct.pack('<fff', *vector) for vector in vectors))

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='''Visualize or generate a Spore .advect file, which is a 128x128 vector force field.\n
        If only the file path is provided, it will visualize an existing .advect file.\n
        If --generate is used, it will evaluate the given NumPy math expression in 'x', 'y' points between 0.0 and 1.0, and generate a .advect file.\n
        Example usage:\n
        \t>> python advect.py test.advect --generate "np.sin(x), 0.1*np.cos(y)"''')
    parser.add_argument('file_path', type=str, help='Path to the .advect file')
    parser.add_argument('--generate', type=str, help='Math expression used to generate an advect force field. Uses "x", "y" as input, and must generate 2 or three numbers')
    args = parser.parse_args()
    
    if args.generate is not None:
        generate_advect(args.file_path, args.generate)

    visualize_advect(args.file_path)
