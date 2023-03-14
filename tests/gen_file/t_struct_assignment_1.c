struct a{
    int i;
    int j;
};

void main(){
    struct a b;
    b.i = 10;
    b.j = 20;
    print_i(b.i + b.j);
}