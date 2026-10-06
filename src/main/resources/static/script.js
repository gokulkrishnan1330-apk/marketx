// ============================================================
// MARKETX - SCRIPT.JS
// ============================================================


// ============================================================
// API
// ============================================================

const API_URL = "/api/cars";


// ============================================================
// GLOBAL SELECTED IMAGE FILES
// ============================================================

let selectedFiles = [];


// ============================================================
// PAGE LOAD
// ============================================================

document.addEventListener("DOMContentLoaded", function () {

    console.log("MarketX script loaded.");

    loadCars();
    checkLoginStatus();
});


// ============================================================
// LOAD ALL CARS
// ============================================================

async function loadCars() {

    const carContainer =
        document.getElementById("carContainer");

    if (!carContainer) {
        return;
    }

    try {

        const response =
            await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load cars.");
        }

        const cars =
            await response.json();

        console.log("Cars:", cars);

        displayCars(cars);

    } catch (error) {

        console.error(
            "Error loading cars:",
            error
        );

        carContainer.innerHTML =
            "<p>Unable to load cars.</p>";
    }
}


// ============================================================
// DISPLAY CARS
// ============================================================

function displayCars(cars) {

    const carContainer =
        document.getElementById("carContainer");

    if (!carContainer) {
        return;
    }

    carContainer.innerHTML = "";

    if (!cars || cars.length === 0) {

        carContainer.innerHTML =
            "<p>No cars found.</p>";

        return;
    }

    cars.forEach(function (car) {

        const card =
            document.createElement("div");

        card.className = "car-card";


        // ------------------------------------------
        // GET FIRST IMAGE
        // ------------------------------------------

        let imageUrl =
            "https://via.placeholder.com/500x300?text=No+Car+Image";

        if (
            Array.isArray(car.images) &&
            car.images.length > 0 &&
            car.images[0] &&
            car.images[0].imageUrl
        ) {

            imageUrl =
                car.images[0].imageUrl;

        } else if (
            car.image &&
            car.image.trim() !== ""
        ) {

            imageUrl =
                car.image;
        }


        // ------------------------------------------
        // CARD
        // ------------------------------------------

        card.innerHTML = `

            <img
                src="${escapeHTML(imageUrl)}"
                alt="${escapeHTML(
                    (car.brand || "") +
                    " " +
                    (car.model || "")
                )}"
                class="car-card-image"
                onerror="this.onerror=null;this.src='https://via.placeholder.com/500x300?text=Image+Not+Available';"
            >

            <div class="car-card-content">

                <h3>
                    ${escapeHTML(
                        (car.brand || "") +
                        " " +
                        (car.model || "")
                    )}
                </h3>

                <p>
                    ${escapeHTML(
                        car.variant || "-"
                    )}
                </p>

                <h4>
                    ₹${Number(
                        car.price || 0
                    ).toLocaleString("en-IN")}
                </h4>

                <p>
                    ${escapeHTML(
                        car.location || "-"
                    )}
                </p>

                <button
                    type="button"
                    onclick="openCarDetails(${car.id})"
                >
                    View Details
                </button>

            </div>
        `;

        carContainer.appendChild(card);
    });
}


// ============================================================
// OPEN CAR DETAILS
// ============================================================

function openCarDetails(carId) {

    if (!carId) {
        return;
    }

    window.location.href =
        "car-details.html?id=" +
        encodeURIComponent(carId);
}


// ============================================================
// SEARCH CARS
// ============================================================

function searchCars() {

    const searchInput =
        document.getElementById("searchInput");

    if (!searchInput) {
        return;
    }

    const searchText =
        searchInput.value
            .trim()
            .toLowerCase();

    const cards =
        document.querySelectorAll(".car-card");

    cards.forEach(function (card) {

        const text =
            card.textContent
                .toLowerCase();

        if (
            searchText === "" ||
            text.includes(searchText)
        ) {

            card.style.display =
                "block";

        } else {

            card.style.display =
                "none";
        }
    });
}


// ============================================================
// ADD CAR
// ============================================================

async function publishCar(event) {

    event.preventDefault();

    console.log("Publish Car started.");


    // ------------------------------------------
    // CHECK LOGIN
    // ------------------------------------------

    const storedUser =
        localStorage.getItem("marketxUser");

    if (!storedUser) {

        alert(
            "Please login before publishing a car."
        );

        window.location.href =
            "login.html";

        return;
    }


    // ------------------------------------------
    // GET FORM
    // ------------------------------------------

    const form =
        document.getElementById("carForm");

    if (!form) {

        alert(
            "Car form not found."
        );

        return;
    }


    // ------------------------------------------
    // GET VALUES
    // ------------------------------------------

    const brand =
        document.getElementById("brand")?.value
            ?.trim() || "";

    const model =
        document.getElementById("model")?.value
            ?.trim() || "";

    const variant =
        document.getElementById("variant")?.value
            ?.trim() || "";

    const fuelType =
        document.getElementById("fuelType")?.value
            ?.trim() || "";

    const transmission =
        document.getElementById("transmission")?.value
            ?.trim() || "";

    const manufacturingYear =
        Number(
            document.getElementById(
                "manufacturingYear"
            )?.value || 0
        );

    const kilometersDriven =
        Number(
            document.getElementById(
                "kilometersDriven"
            )?.value || 0
        );

    const price =
        Number(
            document.getElementById(
                "price"
            )?.value || 0
        );

    const location =
        document.getElementById("location")?.value
            ?.trim() || "";

    const description =
        document.getElementById("description")?.value
            ?.trim() || "";


    // ------------------------------------------
    // VALIDATION
    // ------------------------------------------

    if (!brand) {
        alert("Please select a brand.");
        return;
    }

    if (!model) {
        alert("Please select a model.");
        return;
    }

    if (!variant) {
        alert("Please select a variant.");
        return;
    }

    if (!fuelType) {
        alert("Please select fuel type.");
        return;
    }

    if (!transmission) {
        alert("Please select transmission.");
        return;
    }

    if (manufacturingYear <= 0) {
        alert("Please enter manufacturing year.");
        return;
    }

    if (kilometersDriven < 0) {
        alert("Kilometers cannot be negative.");
        return;
    }

    if (price <= 0) {
        alert("Please enter a valid price.");
        return;
    }

    if (!location) {
        alert("Please enter location.");
        return;
    }


    // ------------------------------------------
    // CHECK IMAGES
    // ------------------------------------------

    if (
        !selectedFiles ||
        selectedFiles.length === 0
    ) {

        const continueWithoutImage =
            confirm(
                "No images selected. Do you want to publish the car without images?"
            );

        if (!continueWithoutImage) {
            return;
        }
    }


    // ------------------------------------------
    // CREATE CAR OBJECT
    // ------------------------------------------

    const carData = {

        brand: brand,

        model: model,

        variant: variant,

        fuelType: fuelType,

        transmission: transmission,

        manufacturingYear:
            manufacturingYear,

        kilometersDriven:
            kilometersDriven,

        price: price,

        location: location,

        description: description,

        image: ""
    };


    // ------------------------------------------
    // DISABLE BUTTON
    // ------------------------------------------

    const submitButton =
        form.querySelector(
            'button[type="submit"]'
        );

    if (submitButton) {

        submitButton.disabled = true;

        submitButton.textContent =
            "Publishing...";
    }


    try {

        // ==================================================
        // STEP 1
        // CREATE CAR
        // ==================================================

        console.log(
            "Creating car:",
            carData
        );

        const carResponse =
            await fetch(API_URL, {

                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(carData)
            });


        if (!carResponse.ok) {

            const errorText =
                await carResponse.text();

            throw new Error(
                errorText ||
                "Unable to create car."
            );
        }


        const savedCar =
            await carResponse.json();

        console.log(
            "Car created:",
            savedCar
        );


        const carId =
            savedCar.id;


        if (!carId) {

            throw new Error(
                "Car ID was not returned by server."
            );
        }


        // ==================================================
        // STEP 2
        // UPLOAD EACH IMAGE
        // ==================================================

        let uploadedImageUrls = [];


        for (
            let i = 0;
            i < selectedFiles.length;
            i++
        ) {

            const imageFile =
                selectedFiles[i];

            console.log(
                "Uploading image:",
                imageFile.name
            );


            // ------------------------------------------
            // CREATE FORM DATA
            // ------------------------------------------

            const imageFormData =
                new FormData();

            imageFormData.append(
                "image",
                imageFile
            );


            // ------------------------------------------
            // UPLOAD
            // ------------------------------------------

            const uploadResponse =
                await fetch(
                    "/api/images/upload",
                    {
                        method: "POST",
                        body: imageFormData
                    }
                );


            if (!uploadResponse.ok) {

                const uploadError =
                    await uploadResponse.text();

                throw new Error(
                    uploadError ||
                    "Image upload failed."
                );
            }


            const imageUrl =
                (
                    await uploadResponse.text()
                ).trim();


            if (!imageUrl) {

                throw new Error(
                    "Server returned an empty image URL."
                );
            }


            console.log(
                "Uploaded image URL:",
                imageUrl
            );


            uploadedImageUrls.push(
                imageUrl
            );


            // ==================================================
            // STEP 3
            // CONNECT IMAGE TO CAR
            // ==================================================

            const imageSaveResponse =
                await fetch(
                    "/api/cars/" +
                    encodeURIComponent(carId) +
                    "/images",
                    {

                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify({
                                imageUrl:
                                    imageUrl
                            })
                    }
                );


            if (
                !imageSaveResponse.ok
            ) {

                const imageSaveError =
                    await imageSaveResponse.text();

                throw new Error(
                    imageSaveError ||
                    "Unable to connect image to car."
                );
            }


            console.log(
                "Image connected to car:",
                imageUrl
            );
        }


        // ==================================================
        // STEP 4
        // OPTIONAL MAIN IMAGE
        // ==================================================

        // We keep the first uploaded image
        // in the legacy Car.image field.
        //
        // The gallery itself uses CarImage records.

        if (
            uploadedImageUrls.length > 0
        ) {

            try {

                const updateResponse =
                    await fetch(
                        "/api/cars/" +
                        encodeURIComponent(carId),
                        {

                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify({
                                    brand: brand,
                                    model: model,
                                    variant: variant,
                                    fuelType: fuelType,
                                    transmission: transmission,
                                    manufacturingYear:
                                        manufacturingYear,
                                    kilometersDriven:
                                        kilometersDriven,
                                    price: price,
                                    location: location,
                                    description: description,
                                    image:
                                        uploadedImageUrls[0]
                                })
                        }
                    );


                if (!updateResponse.ok) {

                    console.warn(
                        "Main image update failed, but gallery images were saved."
                    );
                }

            } catch (updateError) {

                console.warn(
                    "Main image update skipped:",
                    updateError
                );
            }
        }


        // ==================================================
        // SUCCESS
        // ==================================================

        alert(
            "Car published successfully!"
        );


        // Clear selected files

        selectedFiles = [];


        // Reset form

        form.reset();


        // Redirect

        window.location.href =
            "car-details.html?id=" +
            encodeURIComponent(carId);


    } catch (error) {

        console.error(
            "Publish car error:",
            error
        );


        alert(
            error.message ||
            "Unable to publish car."
        );


        // ------------------------------------------
        // BUTTON RESET
        // ------------------------------------------

        if (submitButton) {

            submitButton.disabled =
                false;

            submitButton.textContent =
                "Publish Car";
        }
    }
}


// ============================================================
// IMAGE SELECTION
// ============================================================

function handleImageSelection(event) {

    const files =
        Array.from(
            event.target.files || []
        );


    files.forEach(function (file) {

        // ------------------------------------------
        // VALIDATE TYPE
        // ------------------------------------------

        const allowedTypes = [
            "image/jpeg",
            "image/png",
            "image/webp"
        ];

        if (
            !allowedTypes.includes(
                file.type
            )
        ) {

            alert(
                file.name +
                " is not a supported image."
            );

            return;
        }


        // ------------------------------------------
        // VALIDATE SIZE
        // ------------------------------------------

        const maxSize =
            10 * 1024 * 1024;

        if (file.size > maxSize) {

            alert(
                file.name +
                " is larger than 10 MB."
            );

            return;
        }


        // ------------------------------------------
        // DUPLICATE CHECK
        // ------------------------------------------

        const duplicate =
            selectedFiles.some(
                function (existingFile) {

                    return (
                        existingFile.name ===
                            file.name &&
                        existingFile.size ===
                            file.size &&
                        existingFile.lastModified ===
                            file.lastModified
                    );
                }
            );


        if (duplicate) {
            return;
        }


        // ------------------------------------------
        // ADD FILE
        // ------------------------------------------

        selectedFiles.push(file);
    });


    // ------------------------------------------
    // UPDATE FILE INPUT
    // ------------------------------------------

    updateImageInput();


    // ------------------------------------------
    // DISPLAY PREVIEWS
    // ------------------------------------------

    displayImagePreviews();
}


// ============================================================
// UPDATE FILE INPUT
// ============================================================

function updateImageInput() {

    const imageInput =
        document.getElementById("images");

    if (!imageInput) {
        return;
    }


    const dataTransfer =
        new DataTransfer();


    selectedFiles.forEach(
        function (file) {

            dataTransfer.items.add(
                file
            );
        }
    );


    imageInput.files =
        dataTransfer.files;
}


// ============================================================
// DISPLAY IMAGE PREVIEWS
// ============================================================

function displayImagePreviews() {

    const previewGrid =
        document.getElementById(
            "imagePreviewGrid"
        );

    const selectedImagesContainer =
        document.getElementById(
            "selectedImages"
        );

    const imageCount =
        document.getElementById(
            "imageCount"
        );


    if (!previewGrid) {
        return;
    }


    previewGrid.innerHTML = "";


    selectedFiles.forEach(
        function (file, index) {

            const reader =
                new FileReader();


            reader.onload =
                function (event) {

                    const wrapper =
                        document.createElement(
                            "div"
                        );

                    wrapper.className =
                        "preview-image-wrapper";


                    wrapper.innerHTML = `

                        <img
                            src="${event.target.result}"
                            alt="Selected image ${index + 1}"
                            class="preview-image"
                        >

                        <button
                            type="button"
                            class="remove-image-btn"
                            onclick="removeImage(${index})"
                        >
                            Remove
                        </button>

                    `;


                    previewGrid.appendChild(
                        wrapper
                    );
                };


            reader.readAsDataURL(file);
        }
    );


    // ------------------------------------------
    // IMAGE COUNT
    // ------------------------------------------

    if (imageCount) {

        imageCount.textContent =
            selectedFiles.length +
            " image" +
            (
                selectedFiles.length === 1
                    ? ""
                    : "s"
            ) +
            " selected";
    }


    // ------------------------------------------
    // SELECTED IMAGE LIST
    // ------------------------------------------

    if (selectedImagesContainer) {

        selectedImagesContainer.innerHTML = "";


        selectedFiles.forEach(
            function (file, index) {

                const item =
                    document.createElement(
                        "div"
                    );

                item.className =
                    "selected-image-item";


                item.textContent =
                    (
                        index + 1
                    ) +
                    ". " +
                    file.name;


                selectedImagesContainer
                    .appendChild(item);
            }
        );
    }
}


// ============================================================
// REMOVE IMAGE
// ============================================================

function removeImage(index) {

    if (
        index < 0 ||
        index >= selectedFiles.length
    ) {
        return;
    }


    selectedFiles.splice(
        index,
        1
    );


    updateImageInput();

    displayImagePreviews();
}


// ============================================================
// FILTER CARS
// ============================================================

function filterCars() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );

    const fuelFilter =
        document.getElementById(
            "fuelFilter"
        );

    const transmissionFilter =
        document.getElementById(
            "transmissionFilter"
        );

    const priceFilter =
        document.getElementById(
            "priceFilter"
        );


    const searchText =
        searchInput
            ? searchInput.value
                .trim()
                .toLowerCase()
            : "";


    const fuel =
        fuelFilter
            ? fuelFilter.value
            : "";


    const transmission =
        transmissionFilter
            ? transmissionFilter.value
            : "";


    const price =
        priceFilter
            ? priceFilter.value
            : "";


    const cards =
        document.querySelectorAll(
            ".car-card"
        );


    cards.forEach(
        function (card) {

            const cardText =
                card.textContent
                    .toLowerCase();


            let visible = true;


            if (
                searchText &&
                !cardText.includes(
                    searchText
                )
            ) {

                visible = false;
            }


            if (
                fuel &&
                !cardText.includes(
                    fuel.toLowerCase()
                )
            ) {

                visible = false;
            }


            if (
                transmission &&
                !cardText.includes(
                    transmission.toLowerCase()
                )
            ) {

                visible = false;
            }


            if (price) {

                const priceValue =
                    card.querySelector(
                        "h4"
                    )?.textContent || "";


                const numericPrice =
                    Number(
                        priceValue
                            .replace(
                                /[^0-9.]/g,
                                ""
                            )
                    );


                if (
                    price === "under10" &&
                    numericPrice >= 1000000
                ) {

                    visible = false;
                }


                if (
                    price === "10to20" &&
                    (
                        numericPrice < 1000000 ||
                        numericPrice > 2000000
                    )
                ) {

                    visible = false;
                }


                if (
                    price === "above20" &&
                    numericPrice <= 2000000
                ) {

                    visible = false;
                }
            }


            card.style.display =
                visible
                    ? "block"
                    : "none";
        }
    );
}


// ============================================================
// LOGIN STATUS
// ============================================================

function checkLoginStatus() {

    const navAuth =
        document.getElementById(
            "navAuth"
        );

    if (!navAuth) {
        return;
    }


    const storedUser =
        localStorage.getItem(
            "marketxUser"
        );


    if (!storedUser) {

        navAuth.innerHTML = `

            <a
                href="login.html"
                class="login-btn"
            >
                Login
            </a>

            <a
                href="register.html"
                class="register-btn"
            >
                Register
            </a>

        `;

        return;
    }


    let userData = null;


    try {

        userData =
            JSON.parse(
                storedUser
            );

    } catch (error) {

        console.error(
            "Invalid marketxUser:",
            error
        );

        localStorage.removeItem(
            "marketxUser"
        );

        return;
    }


    const userName =
        userData.name ||
        userData.email ||
        "User";


    navAuth.innerHTML = `

        <a
            href="profile.html"
            class="profile-btn"
        >
            ${escapeHTML(userName)}
        </a>

        <button
            type="button"
            class="logout-btn"
            onclick="logoutUser()"
        >
            Logout
        </button>

    `;
}


// ============================================================
// LOGOUT
// ============================================================

function logoutUser() {

    localStorage.removeItem(
        "marketxUser"
    );

    window.location.href =
        "index.html";
}


// ============================================================
// CONTACT SELLER
// ============================================================

function contactSeller(carId) {

    if (!carId) {

        alert(
            "Car information not available."
        );

        return;
    }


    const storedUser =
        localStorage.getItem(
            "marketxUser"
        );


    if (!storedUser) {

        alert(
            "Please login to contact the seller."
        );

        window.location.href =
            "login.html";

        return;
    }


    window.location.href =
        "contact-seller.html?id=" +
        encodeURIComponent(carId);
}


// ============================================================
// HTML ESCAPE
// ============================================================

function escapeHTML(value) {

    return String(
        value ?? ""
    )
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}