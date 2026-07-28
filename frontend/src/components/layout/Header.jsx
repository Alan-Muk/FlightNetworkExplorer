export default function Header() {


    return (

        <header

            style={{

                position:"absolute",

                top:0,

                left:0,

                width:"100%",

                height:"60px",

                zIndex:3000,

                display:"flex",

                alignItems:"center",

                padding:"0 25px",

                boxSizing:"border-box",

                background:
                    "rgba(0,0,0,0.85)",

                borderBottom:
                    "1px solid #00ffff",

                color:"white"

            }}

        >

            <h2

                style={{

                    margin:0,

                    color:"#00ffff",

                    letterSpacing:"2px"

                }}

            >

                ✈ Flight Network Explorer

            </h2>


        </header>

    );

}