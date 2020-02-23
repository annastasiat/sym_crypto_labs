import numpy as np
import matplotlib.pyplot as plt
import sys

def plot_IC_all(filename, plot_file_name):
    rs = [2,3,4,5,15]
    colors = ['yellowgreen','chartreuse', 'orange', 'gold',  'blue']
    #colors = ['darkturquoise', 'cadetblue','deepskyblue', 'turquoise', 'yellowgreen']
    plt.figure(figsize=(8,4))
    for i in range(len(rs)):
        data = np.genfromtxt(filename + str(rs[i]) + '_ic.tsv', delimiter='\t')
        plt.bar(data.T[0]+0.2*i, data.T[1], width=0.2, color=colors[i], label='r='+str(rs[i]))
        plt.xticks(data.T[0])
    plt.legend()
    plt.title("Indexes of coincidence")
    plt.xlabel('r')
    plt.ylabel('I(Y)')
    plt.savefig(plot_file_name, dpi=300)


plot_IC_all(sys.argv[1], sys.argv[2])
#plot_IC_all("lab2/task12/test", "lab2/task12/test.png")

