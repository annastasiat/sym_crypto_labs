import numpy as np
import matplotlib.pyplot as plt
import sys



def plot_IC(filename, plot_file_name):
    #plt.scatter(data.T[0], data.T[1], color='red')

    data = np.genfromtxt(filename, delimiter='\t')
    plt.figure(figsize=(8,4))
    plt.bar(data.T[0], data.T[1])
    plt.xticks(data.T[0])
    plt.title("Index of coincidence")
    plt.xlabel('r')
    plt.ylabel('I(Y)')
    plt.savefig(plot_file_name, dpi=300)

plot_IC(sys.argv[1], sys.argv[2])