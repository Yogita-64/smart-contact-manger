console.log("Script Loaded..."); // Debugging: Check if script is loading

const search = () => {
    let query = document.getElementById("search-input").value;

    if (query === "") {
        document.querySelector(".search-result").style.display = "none";
    } else {
        console.log("Searching for:", query);

        let url = `http://localhost:8282/search/${query}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                let text = `<div class='list-group'>`;

                data.forEach(contact => {
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`;
                });

                text += `</div>`;

                document.querySelector(".search-result").innerHTML = text;
                document.querySelector(".search-result").style.display = "block";
            });
    }
};
