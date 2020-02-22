import numpy as np
import matplotlib.pyplot as plt
import sys

def plot_IC_all(filename, plot_file_name):
    rs = [2,3,4,5,15]
    k=0
    colors = ['yellowgreen','chartreuse', 'orange', 'gold',  'blue']
    #colors = ['darkturquoise', 'cadetblue','deepskyblue', 'turquoise', 'yellowgreen']
    plt.figure(figsize=(8,4))
    for i in range(len(rs)):
        data = np.genfromtxt(filename + str(rs[i]) + '.tsv', delimiter='\t')
        plt.bar(data.T[0]+0.2*i, data.T[1], width=0.2, color=colors[i], label='r='+str(rs[i]))
        plt.xticks(data.T[0])
    plt.legend()
    plt.title("Index of coincidence")
    plt.xlabel('r')
    plt.ylabel('I(Y)')
    plt.savefig(plot_file_name, dpi=300)


plot_IC_all("lab2/test", "lab2/task12/test.png")


'''
def plot_mul_boringmul():
    data1 = np.genfromtxt('data/GFNBmul.csv', delimiter=',')
    data2 = np.genfromtxt('data/GFNBboringMul.csv', delimiter=',')

    p13 = np.poly1d(np.polyfit(data1.T[0], data1.T[1], 3), variable='m')
    p12 = np.poly1d(np.polyfit(data1.T[0], data1.T[1], 2), variable='m')
    p23 = np.poly1d(np.polyfit(data2.T[0], data2.T[1], 3), variable='m')
    p22 = np.poly1d(np.polyfit(data2.T[0], data2.T[1], 2), variable='m')

    plt.plot(data1.T[0],p13(data1.T[0]), '-',color='yellowgreen', label=p13)
    plt.plot(data1.T[0],p12(data1.T[0]), '-',color='chartreuse', label=p12)

    plt.plot(data2.T[0],p23(data2.T[0]), '-',color='orange', label=p23)
    plt.plot(data2.T[0],p22(data2.T[0]), '-',color='yellow', label=p22)

    plt.plot(data1.T[0],data1.T[1], color='blue', label="mul")
    plt.plot(data2.T[0],data2.T[1], color='red', label="boring mul")

    plt.title("GFNBmul")
    plt.xlabel('m')
    plt.ylabel('time, millisec')
    plt.grid(True)
    plt.legend()
    plt.savefig('data_plots/GFNBmul_boringMul.png', dpi=300)




plot_inv()
'''

