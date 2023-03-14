void main(){
    int a[2];
    int b[3];
    a[0] = 10;
    b[0] = a[0];
    b[1] = 20;
    a[0] = b[1];
    print_i(a[0] + b[1]);
}