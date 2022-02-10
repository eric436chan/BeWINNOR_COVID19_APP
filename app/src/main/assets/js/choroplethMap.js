/*
    Makes a D3.js Choropleth Map based on parameters passed
*/
const makeChoroplethMap = (inData, inStates, inTotal, inType, inHeight, inWidth) => {
    // Creates a data and state array from strings
    const dataArray = (inData.substr(1, inData.length - 2)).split(",").map(data => data.replace(" ", ""));
    const statesArray = (inStates.substr(1, inStates.length - 2)).split(",").map(state => state.substr(1));

    // Maps state and total as ("California", (californiaTotal/USTotal) * 30)
    const states = new Map();
    statesArray.forEach((state, i) => {
        states.set(state, (parseInt(dataArray[i]) / parseInt(inTotal) * 30))
    });

    // Maps the remaining 5 states not included in the data visualization
    states.set("District of Columbia", 0);
    states.set("American Samoa", 0);
    states.set("Guam", 0);
    states.set("Commonwealth of the Northern Mariana Islands", 0);
    states.set("Puerto Rico", 0);
    states.set("United States Virgin Islands", 0);

    // Calculate margin, width and height
    const margin = {
        top: parseInt(inHeight) * 0.15,
        left: parseInt(inWidth) * -0.075,
    };
    const width = parseInt(inWidth) - margin.left;
    const height = parseInt(inHeight) - margin.top;

    // Create svg
    const svg = d3
        .select("body")
        .append("svg")
        .attr("width", width + margin.left)
        .attr("height", height + margin.top);

    // Create rect
    svg.append("rect")
        .attr("width", width * 2)
        .attr("height", height * 2)
        .attr("fill", "white")
        .on("mouseenter", function () {
            // Calls kotlin function when rect clicked (outside of map)
            kotlin.setCountryText();
        });

    // Create g container for the map
    const g = svg.append("g")
        .attr("transform", `translate(${margin.left}, ${margin.top})`);

    // Get projection and path
    const projection = d3.geoAlbersUsa();
    const path = d3.geoPath(projection);

    // Get color
    const color = (inType == "INFECTED") ? d3.scaleSequential(d3.interpolateOranges) :
        (inType == "DEATHS") ? d3.scaleSequential(d3.interpolateReds) :
        d3.scaleSequential(d3.interpolateGreens);

    //Get json from US ATLAS for US states coordinates
    d3.json("https://cdn.jsdelivr.net/npm/us-atlas/states-10m.json").then((us) => {

        const statesTopography = topojson.feature(us, us.objects.states).features;
        const nationTopography = topojson.feature(us, us.objects.nation).features[0];

        // Make projection fit the svg dimensions
        projection.fitSize([width, height], nationTopography);

        // Add values of each state from map
        const totals = statesTopography.map((feature) => ({
            feature: feature,
            name: feature.properties.name,
            value: states.get(feature.properties.name)
        }));

        // Add all paths to g
        g.selectAll("path")
            .data(totals)
            .join((enter) => {
                const p = enter.append("path");
                p.on("mouseenter", function () {
                    // Bring element to the front
                    d3.select(this).raise();
                    // Calls kotlin function when path clicked
                    kotlin.setStateText(`${d3.select(this)._groups[0][0].__data__.name}`);
                });
                return p;
            })
            .attr("d", (d) => path(d.feature))
            .style("fill", (d) => color(d.value));
    });
}