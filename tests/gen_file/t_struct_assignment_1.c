struct a{
    int i;
    int j;
};
struct a b;

void main(){

    b.i = 10;
    b.j = 20;
    print_i(b.i + b.j);
}