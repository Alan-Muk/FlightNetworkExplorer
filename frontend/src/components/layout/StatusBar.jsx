export default function StatusBar({
    airport,
    routes
}) {


    return (

        <footer

            style={{

                position:"absolute",

                bottom:0,

                left:0,

                width:"100%",

                height:"45px",

                zIndex:3000,

                display:"flex",

                alignItems:"center",

                padding:"0 20px",

                boxSizing:"border-box",

                background:
                    "rgba(0,0,0,0.85)",

                borderTop:
                    "1px solid #00ffff",

                color:"white"

            }}

        >

            {
                airport ?

                (

                    <>
                        Selected:

                        {" "}

                        <b
                            style={{
                                color:"#00ffff",
                                marginLeft:"5px"
                            }}
                        >
                            {airport.iata}
                        </b>


                        <span
                            style={{
                                marginLeft:"20px"
                            }}
                        >
                            Routes:
                            {" "}
                            {routes}
                        </span>

                    </>

                )

                :

                (

                    <>
                        Select an airport to explore
                    </>

                )
            }


        </footer>

    );

}