<template>
  <div v-if="isReady" class="container" style="font-family: Rajdhani, sans-serif; color: #12044f; font-weight: 700; margin-bottom: 100px; margin-top: 120px;">
    <div class="text-center text-uppercase pb-3">
      <h3>Category Update Form</h3>
      <hr class="mx-auto" style="width: 50px; border-top: 3px solid #12044f" />
    </div>

    <div class="row justify-content-center">
      <div class="col-lg-6 col-md-8">
        <div class="card shadow-sm p-4">
          <form id="roomCategoryForm">
            <div class="mb-3">
              <label class="form-label">Name</label>
              <input type="text" class="form-control" id="name" name="name" placeholder="Enter Category Name" :value="currentCategory.name" />
              <span class="text-danger" id="nameError" style="visibility: hidden">Not empty! Max 30 letters allowed!</span>
            </div>

            <div class="mb-3">
              <label class="form-label">Price</label>
              <input type="text" class="form-control" id="price" name="price" placeholder="Enter Price" :value="currentCategory.price" @keydown="validationService.validateNumber($event)" />
              <span class="text-danger" id="priceError" style="visibility: hidden">Please provide valid price!</span>
            </div>

            <div class="mb-3" v-for="feature in ['wc', 'wifi', 'tv', 'bar']" :key="feature">
              <label class="form-label">{{ feature.toUpperCase() }}</label>
              <select class="form-select" :id="feature" :name="feature">
                <option :value="''">Please Select</option>
                <option :value="'0'">No</option>
                <option :value="'1'">Yes</option>
              </select>
              <span class="text-danger" :id="`${feature}Error`" style="visibility: hidden">Please select one of the options!</span>
            </div>

            <div class="text-center">
              <button type="submit" class="btn btn-info px-4">Update</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="text-center mt-5">
    <div class="spinner-border text-primary mt-5" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
</template>

<script lang="ts">
import RoomCategory from "@/classes/RoomCategory";
import RoomCategoryService from "@/services/RoomCategoryService";
import ValidationService from "@/services/ValidationService";
import axios from "axios";
import { defineComponent } from "vue";
import { useRoute } from "vue-router";

export default defineComponent({
  name: "CategoryUpdateView",

  data() {
    return {
      isReady: false,
      currentCategory: new RoomCategory(),
      categoryService: new RoomCategoryService(),
      validationService: new ValidationService(),
      route: useRoute(),
    };
  },

  methods: {
    getCategoryDetails(categoryId: any): Promise<void> {
      return this.categoryService
        .getCategoryDetails(categoryId)
        .then((response) => {
          const data = response?.data;
          if (!data) {
            console.warn("No category data received.");
            return;
          }

          this.currentCategory = data;

          const fields = ["wc", "wifi", "tv", "bar"] as const;
          fields.forEach((field) => {
            const el = document.getElementById(field) as HTMLSelectElement | null;
            const val = this.currentCategory[field];
            if (el && val !== undefined && val !== null) {
              el.value = val.toString();
            }
          });
        })
        .catch((err) => {
          console.error("Failed to load category:", err);
        });
    },

    handleFormSubmission(): void {
      const form = document.getElementById("roomCategoryForm") as HTMLFormElement;
      form?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const formData = new FormData(form);
        const serializedData: { [key: string]: string } = {};
        formData.forEach((value, key) => {
          serializedData[key] = value.toString().trim();
        });

        if (this.validationService.validateRoomCategory()) {
          try {
            await axios.put(`${this.categoryService.getTargetUrl()}/${this.currentCategory.roomCategoryId}`, {
              name: serializedData.name,
              price: Number(serializedData.price),
              wc: Number(serializedData.wc),
              wifi: Number(serializedData.wifi),
              tv: Number(serializedData.tv),
              bar: Number(serializedData.bar),
            });
            this.categoryService.redirectAllCategories();
          } catch (error) {
            console.error("Update failed:", error);
          }
        }
      });
    }
  },

  created() {
    this.getCategoryDetails(this.route.params.categoryId)
      .then(() => {
        this.isReady = true;
      })
      .catch((err) => {
        console.error("Error loading form:", err);
      });
  },

  mounted() {
    const interval = setInterval(() => {
      const form = document.getElementById("roomCategoryForm");
      if (form && this.isReady) {
        clearInterval(interval);
        this.handleFormSubmission();
      }
    }, 100);
  }
});
</script>
