
struct a{
    int i;
};

struct a foo(){
    struct a b;
    return b;
}

void main(){
    foo().i;

}

int bane(){
    int b[1][2];
    return b[1][2];
}

