int a[2];

void main(){
    int b[2];
    b[0] = 10;
    b[1] = 20;
    a = b; // this is illegal;
    print_i(a[0] + a[1]);
}